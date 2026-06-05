/**
 * Tests the routing logic of {@link GeneratorResolver}.
 * Ensures that numeric types are correctly intercepted and routed to the 
 * custom Gaussian generator, while unsupported types gracefully fall back to standard generators.
 */

package org.instancio.internal.generator;

import org.instancio.generator.Generator;
import org.instancio.internal.nodes.InternalNode;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneratorResolverTest {

    private InternalNode createMockNode(Class<?> targetClass) {
        InternalNode mockNode = mock(InternalNode.class);
        when(mockNode.getTargetClass()).thenReturn((Class) targetClass);
        return mockNode;
    }

    @Test
    void shouldRouteToGaussianGeneratorWhenFeatureIsEnabledAndValid() {
    
        Settings settings = Settings.defaults()
                .set(Keys.GAUSSIAN_ENABLED, true)
                .set(Keys.GAUSSIAN_MEAN, 30.0)
                .set(Keys.GAUSSIAN_SD, 5.0)
                .set(Keys.GAUSSIAN_MIN, 10.0)
                .set(Keys.GAUSSIAN_MAX, 50.0);

        InternalGeneratorContext context = new InternalGeneratorContext(settings, null);
        GeneratorResolver resolver = new GeneratorResolver(context);
        
        InternalNode doubleNode = createMockNode(Double.class);

        Generator<?> generator = resolver.get(doubleNode);

        assertThat(generator.getClass().getSimpleName())
                .as("Traffic cop should route to the custom Gaussian lambda, not the standard generator")
                .doesNotContain("DoubleGenerator");
    }

    @Test
    void shouldRouteToStandardGeneratorForUnsupportedNumericTypes() {
        
        Settings settings = Settings.defaults()
                .set(Keys.GAUSSIAN_ENABLED, true)
                .set(Keys.GAUSSIAN_MEAN, 30.0)
                .set(Keys.GAUSSIAN_SD, 5.0)
                .set(Keys.GAUSSIAN_MIN, 10.0)
                .set(Keys.GAUSSIAN_MAX, 50.0);

        InternalGeneratorContext context = new InternalGeneratorContext(settings, null);
        GeneratorResolver resolver = new GeneratorResolver(context);
        
        InternalNode bigDecimalNode = createMockNode(java.math.BigDecimal.class);
        Generator<?> generator = resolver.get(bigDecimalNode);

        assertThat(generator.getClass().getSimpleName())
                .as("Unsupported numeric types should bypass the Gaussian interceptor and fallback to standard generators")
                .contains("BigDecimalGenerator");
    }
}