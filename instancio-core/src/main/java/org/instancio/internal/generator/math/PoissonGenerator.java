/*
 * Copyright 2022-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.instancio.internal.generator.math;

import org.instancio.Random;
import org.instancio.generator.Generator;
import org.instancio.generator.GeneratorContext;
import org.instancio.settings.Keys;

/**
 * Generator for producing Poisson distributed random values.
 * <p>
 * This generator uses the {@link Keys#POISSON_LAMBDA} setting
 * to determine the mean (lambda) of the distribution.
 */
public class PoissonGenerator implements Generator<Integer> {

    private final GeneratorContext context;

    // Default variable for the algorithm (Can be fetched from context later)
    private double mean = 5.0;

    public PoissonGenerator(final GeneratorContext context) {
        this.context = context;
    }

    @Override
    public Integer generate(Random random) {
        // 1. Validate parameters
        if (this.mean <= 0) {
            throw new IllegalArgumentException("Mean (lambda) must be strictly positive");
        }

        // 2. Initial values for Knuth's algorithm
        double limit = Math.exp(-this.mean);
        double p = 1.0;
        int k = 0;

        // 3. Loop to generate random numbers until reaching the target distribution
        do {
            k++;
            // Instancio uses doubleRange(min, max) instead of nextDouble()
            double u = random.doubleRange(0.0, 1.0);
            p *= u;
        } while (p > limit);

        // 4. Return the calculated Poisson value
        return k - 1;
    }
}