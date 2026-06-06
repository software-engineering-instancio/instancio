package org.instancio;

import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TruncatedGaussianIntegrationTest {

    @Test
    void shouldGenerateGaussianDoublesWhenEnabledViaPublicApi() {
        Settings settings = Settings.create()
                .set(Keys.GAUSSIAN_ENABLED, true)
                .set(Keys.GAUSSIAN_MEAN, 50.0)
                .set(Keys.GAUSSIAN_SD, 10.0)
                .set(Keys.GAUSSIAN_MIN, 20.0)
                .set(Keys.GAUSSIAN_MAX, 80.0);

        // Initialize the engine once and generate a bulk list of 1000 values for optimal performance
        List<Double> results = Instancio.ofList(Double.class)
                .size(1000)
                .withSettings(settings)
                .create();

        assertThat(results)
                .as("All generated values via public API must respect Gaussian boundaries")
                .allSatisfy(result -> assertThat(result).isBetween(20.0, 80.0));
    }
    
    @Test
    void shouldNotApplyGaussianWhenFeatureIsDisabledViaPublicApi() {
        Settings settings = Settings.create()
                .set(Keys.GAUSSIAN_ENABLED, false)
                .set(Keys.GAUSSIAN_MEAN, 50.0)
                .set(Keys.GAUSSIAN_MIN, 20.0)
                .set(Keys.GAUSSIAN_MAX, 80.0);

        List<Double> results = Instancio.ofList(Double.class)
                .size(1000)
                .withSettings(settings)
                .create();

        // If any element falls outside the narrow bounds, it proves the standard generator was used
        boolean generatedOutsideBounds = results.stream()
                .anyMatch(result -> result < 20.0 || result > 80.0);
        
        assertThat(generatedOutsideBounds)
                .as("Standard generator should eventually produce numbers outside our narrow unused bounds")
                .isTrue();
    }
}
