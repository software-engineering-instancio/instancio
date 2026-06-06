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

import org.instancio.Random;
import org.instancio.generator.GeneratorContext;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;

public record InternalGeneratorContext(Settings settings, Random random)
        implements GeneratorContext {
/**
     * Safely retrieves the outlier probability from the current settings.
     * If the setting is missing or null, it defaults to 0.0 (disabled).
     *
     * @return the probability of generating an outlier
     */
    public double getOutlierProbability() {
        Double probability = settings().get(Keys.OUTLIER_PROBABILITY);
        if (probability != null) {
            return probability;
        }
        return 0.0;
    }

    /**
     * Safely retrieves the outlier severity multiplier from the settings.
     * If the setting is missing or null, it defaults to a standard 10.0 multiplier.
     *
     * @return the severity multiplier for the anomaly
     */
    public double getOutlierSeverity() {
        Double severity = settings().get(Keys.OUTLIER_SEVERITY);
        if (severity != null) {
            return severity;
        }
        return 10.0;
    }
    /**
     * Safely retrieves the Gaussian mean from the settings.
     * Defaults to 0.0 if not explicitly configured by the user.
     *
     * @return the configured or default mean
     */
    public double getGaussianMean() {
        Double mean = settings().get(Keys.GAUSSIAN_MEAN);
        return mean != null ? mean : 0.0;
    }

    /**
     * Safely retrieves the Gaussian standard deviation from the settings.
     * Defaults to 1.0 if not configured or if an invalid negative value is provided.
     *
     * @return the configured or default standard deviation
     */
    public double getGaussianSd() {
        Double sd = settings().get(Keys.GAUSSIAN_SD);
        return (sd != null && sd > 0.0) ? sd : 1.0;
    }

    /**
     * Validates the Gaussian distribution boundary settings safely.
     * This helper method ensures that any generator requesting these parameters
     * receives mathematically valid boundaries, preventing runtime exceptions.
     *
     * @return true if Gaussian generation is enabled and boundaries are valid
     */
    public boolean hasValidGaussianConfiguration() {
        final Boolean isEnabled = settings().get(Keys.GAUSSIAN_ENABLED);
        if (!Boolean.TRUE.equals(isEnabled)) {
            return false;
        }

        final Double min = settings().get(Keys.GAUSSIAN_MIN);
        final Double max = settings().get(Keys.GAUSSIAN_MAX);

        // Ensure boundaries are provided and logically correct (min < max)
        return min != null && max != null && min < max;
    }

}
