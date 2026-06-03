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
 * Generator for producing Binomial distributed random values.
 * <p>
 * This generator requires the {@link Keys#BINOMIAL_TRIALS} and
 * {@link Keys#BINOMIAL_PROBABILITY} settings to operate.
 */
public class BinomialGenerator implements Generator<Integer> {

    private final GeneratorContext context;
    private final int trials;
    private final double probability;

    public BinomialGenerator(final GeneratorContext context) {
        this.context = context;
        // getSettings() yerine Instancio'nun güncel metodu olan settings() kullanıyoruz
        this.trials = context.settings().get(Keys.BINOMIAL_TRIALS);
        this.probability = context.settings().get(Keys.BINOMIAL_PROBABILITY);
    }

    @Override
    public Integer generate(Random random) {
        // 1. Validate parameters to ensure they are within mathematically acceptable ranges
        if (this.trials < 0 || this.probability < 0.0 || this.probability > 1.0) {
            throw new IllegalArgumentException("Trials must be >= 0 and probability must be between 0.0 and 1.0");
        }

        int successes = 0;

        // 2. Iterate for the total number of trials
        for (int i = 0; i < this.trials; i++) {
            // 3. Count as a success if the generated random number is less than the probability
            if (random.doubleRange(0.0, 1.0) < this.probability) {
                successes++;
            }
        }

        // 4. Return the total number of successful trials
        return successes;
    }
}