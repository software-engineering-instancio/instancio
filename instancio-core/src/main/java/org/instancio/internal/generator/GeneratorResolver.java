/*
 * Copyright 2022-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.instancio.internal.generator;

import org.instancio.generator.Generator;
import org.instancio.generator.GeneratorContext;
import org.instancio.generator.specs.SubtypeGeneratorSpec;
import org.instancio.internal.generator.array.ArrayGenerator;
import org.instancio.internal.generator.lang.EnumGenerator;
import org.instancio.internal.generator.util.CollectionGenerator;
import org.instancio.internal.generator.util.MapGenerator;
import org.instancio.internal.nodes.InternalNode;
import org.instancio.internal.nodes.NodeKind;
import org.instancio.internal.util.ReflectionUtils;
import org.instancio.internal.util.Sonar;
import org.instancio.settings.Keys;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static org.instancio.internal.generator.GeneratorUtil.instantiateInternalGenerator;

@SuppressWarnings(Sonar.GENERIC_WILDCARD_IN_RETURN)
public class GeneratorResolver {

    private final GeneratorContext context;
    private final Map<Class<?>, @Nullable Generator<?>> cache = new HashMap<>();

    public GeneratorResolver(final GeneratorContext context) {
        this.context = context;
    }

    /**
     * Returns a generator for the given {@code node}.
     * This method returns a new generator instance on each call.
     *
     * @see #getCached(InternalNode)
     */
    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Generator<?> get(final InternalNode node) {
        final Class<?> klass = node.getTargetClass();
        Generator<?> gaussianGenerator = interceptGaussian(klass);
        if (gaussianGenerator != null) {
            return gaussianGenerator;
        }

        Generator<?> generator = getBuiltInGenerator(klass);
        

        if (generator == null) {
            if (klass.isArray()) {
                generator = new ArrayGenerator<>(context, klass);
            } else if (klass.isEnum()) {
                generator = new EnumGenerator(context, klass);
            } else if (node.is(NodeKind.MAP)) {
                generator = new MapGenerator<>(context).subtype(node.getTargetClass());
            } else if (node.is(NodeKind.COLLECTION)) {
                generator = new CollectionGenerator<>(context).subtype(node.getTargetClass());
            }
        }
        return generator;
    }

    /**
     * Since this method returns a cached generator,
     * callers must not update the generator's state.
     *
     * @see #get(InternalNode)
     */
    @Nullable
    @SuppressWarnings(Sonar.MAP_COMPUTE_IF_ABSENT)
    public Generator<?> getCached(final InternalNode node) {
        final Class<?> targetClass = node.getTargetClass();

        Generator<?> generator = cache.get(targetClass);

        if (generator == null) {
            generator = get(node);
            cache.put(targetClass, generator);
        }

        return generator;
    }

    /**
     * Returns a cached built-in generator for the given class.
     *
     * <p>Unlike {@link #getCached(InternalNode)}, this method resolves
     * <b>only</b> built-in generators registered in {@link GeneratorResolverMaps}.
     *
     * <p>Since this method returns a cached generator,
     * callers must not update the generator's state.
     */
    @Nullable
    @SuppressWarnings(Sonar.MAP_COMPUTE_IF_ABSENT)
    public Generator<?> getCachedBuiltInGenerator(final Class<?> targetClass) {
        Generator<?> generator = cache.get(targetClass);

        if (generator == null) {
            generator = getBuiltInGenerator(targetClass);
            cache.put(targetClass, generator);
        }

        return generator;
    }

    @Nullable
    private Generator<?> getBuiltInGenerator(final Class<?> targetClass) {
        final Class<?> genClass = GeneratorResolverMaps.getGenerator(targetClass);
        if (genClass == null) {
            return getGeneratorForLegacyClass(targetClass);
        }

        final Generator<?> generator = instantiateInternalGenerator(genClass, context);
        if (generator instanceof SubtypeGeneratorSpec<?> subtypeSpec) {
            final Class<?> subtype = GeneratorResolverMaps.getSubtype(targetClass);
            if (subtype != null) {
                subtypeSpec.subtype(subtype);
            }
        }
       // Outlier Injection Logic for Numeric types
        if (generator != null && isNumericType(targetClass)) {
            final Generator<?> originalGen = generator;
            return new Generator<Number>() {
                @Override
                public Number generate(org.instancio.Random random) {
                    Number baseValue = (Number) originalGen.generate(random);

                    double prob = ((InternalGeneratorContext) context).getOutlierProbability();
                    if (prob > 0.0 && random.doubleRange(0.0, 1.0) <= prob) {
                        double severity = ((InternalGeneratorContext) context).getOutlierSeverity();
                        // Randomly decide upper or lower bound anomaly
                        double multiplier = random.trueOrFalse() ? severity : -severity;
                        double outlierValue = baseValue.doubleValue() * multiplier;
                        
                        // Create an artificial spike if the original value was 0
                        if (outlierValue == 0) {
                            outlierValue = multiplier * 100;
                        }
                        
                        return castToOriginalNumericType(outlierValue, targetClass);
                    }
                    return baseValue;
                }

                @Override
                public org.instancio.generator.Hints hints() {
                    return java.util.Objects.requireNonNull(originalGen.hints()); 
                }
            };
        }
        return generator;
    }

    /**
     * Java modules will not have these legacy classes unless explicitly
     * imported via e.g. "requires java.sql". In case the classes are not
     * available, load them by name to avoid class not found error
     */
    @Nullable
    @SuppressWarnings(Sonar.USE_INSTANCEOF)
    private Generator<?> getGeneratorForLegacyClass(final Class<?> klass) {
        final String className = klass.getName();

        if ("java.sql.Date".equals(className)) {
            return loadByClassName(context, "org.instancio.internal.generator.sql.SqlDateGenerator");
        }
        if ("java.sql.Timestamp".equals(className)) {
            return loadByClassName(context, "org.instancio.internal.generator.sql.TimestampGenerator");
        }
        if ("javax.xml.datatype.XMLGregorianCalendar".equals(className)) {
            return loadByClassName(context, "org.instancio.internal.generator.xml.XMLGregorianCalendarGenerator");
        }
        if ("javax.xml.namespace.QName".equals(className)) {
            return loadByClassName(context, "org.instancio.internal.generator.xml.QNameGenerator");
        }
        return null;
    }

    private static Generator<?> loadByClassName(
            final GeneratorContext context,
            final String generatorClassName) {

        final Class<?> generatorClass = ReflectionUtils.loadRequiredClass(generatorClassName);
        return instantiateInternalGenerator(generatorClass, context);
    }
    private boolean isNumericType(Class<?> targetClass) {
        return Number.class.isAssignableFrom(targetClass) || 
               targetClass == int.class || targetClass == long.class || 
               targetClass == double.class || targetClass == float.class || 
               targetClass == short.class || targetClass == byte.class;
    }

    private Number castToOriginalNumericType(double value, Class<?> targetClass) {
        if (targetClass == Integer.class || targetClass == int.class) return (int) value;
        if (targetClass == Long.class || targetClass == long.class) return (long) value;
        if (targetClass == Float.class || targetClass == float.class) return (float) value;
        if (targetClass == Short.class || targetClass == short.class) return (short) value;
        if (targetClass == Byte.class || targetClass == byte.class) return (byte) value;
        return value;
    }
    /**
     * Intercepts numeric generation requests and routes them to the Gaussian logic if the Gaussian configuration is present in the settings.
     */
    @Nullable
    private Generator<?> interceptGaussian(final Class<?> targetClass) {
        // Restrict Gaussian generation to explicitly supported primitive and wrapper numeric types.
        // This acts as a strict shield against unsupported types to prevent ClassCastException downstream.
        
        final boolean isSupportedNumeric = targetClass == double.class || targetClass == Double.class
                || targetClass == float.class || targetClass == Float.class
                || targetClass == int.class || targetClass == Integer.class
                || targetClass == long.class || targetClass == Long.class
                || targetClass == short.class || targetClass == Short.class
                || targetClass == byte.class || targetClass == Byte.class;

        if (isSupportedNumeric && context instanceof InternalGeneratorContext internalContext) {
            // Proceed only if the user has explicitly enabled the feature and provided logical boundaries
            if (internalContext.hasValidGaussianConfiguration()) {
                
                final double mean = internalContext.getGaussianMean();
                final double sd = internalContext.getGaussianSd();
                final double min = context.settings().get(Keys.GAUSSIAN_MIN);
                final double max = context.settings().get(Keys.GAUSSIAN_MAX);

                return new Generator<Object>() {
                    @Override
                    public Object generate(org.instancio.Random random) {
                        
                 
                        if (!(random instanceof org.instancio.support.DefaultRandom defaultRandom)) {
                            throw new IllegalStateException("Gaussian generation requires DefaultRandom implementation");
                        }

                       
                        final double value = defaultRandom.nextTruncatedGaussian(mean, sd, min, max);

                        // Safely cast the resulting double to the exact requested target class
                        if (targetClass == int.class || targetClass == Integer.class) return (int) value;
                        if (targetClass == long.class || targetClass == Long.class) return (long) value;
                        if (targetClass == float.class || targetClass == Float.class) return (float) value;
                        if (targetClass == short.class || targetClass == Short.class) return (short) value;
                        if (targetClass == byte.class || targetClass == Byte.class) return (byte) value;
                        
                        return value; 
                    }

                    @Override
                    public org.instancio.generator.Hints hints() {
                        // Provide an empty hints object to satisfy Instancio's internal engine requirements and prevent NullPointerException during the generation pipeline.
                        return org.instancio.generator.Hints.builder().build();
                    }
                };
            }
        }

        return null;
    }
}
