package org.instancio.internal.generator;

import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InternalGeneratorContextTest {

    private InternalGeneratorContext createContext(Settings settings) {
        // The Gaussian methods only require Settings. 
        // We can safely pass null for the Random instance.
        return new InternalGeneratorContext(settings, null);
    }

    @Test
    void shouldReturnTrueWhenGaussianConfigurationIsValid() {
        Settings settings = Settings.create()
                .set(Keys.GAUSSIAN_ENABLED, true)
                .set(Keys.GAUSSIAN_MIN, 10.0)
                .set(Keys.GAUSSIAN_MAX, 50.0);

        InternalGeneratorContext context = createContext(settings);

        assertThat(context.hasValidGaussianConfiguration())
                .as("Valid boundaries and enabled feature should return true")
                .isTrue();
    }

    @Test
    void shouldReturnFalseWhenMinIsGreaterThanMax() {
        Settings settings = Settings.create()
                .set(Keys.GAUSSIAN_ENABLED, true)
                .set(Keys.GAUSSIAN_MIN, 100.0)
                .set(Keys.GAUSSIAN_MAX, 50.0);

        InternalGeneratorContext context = createContext(settings);

        assertThat(context.hasValidGaussianConfiguration())
                .as("Shield should block invalid boundaries where min is greater than max")
                .isFalse();
    }

    @Test
    void shouldReturnFalseWhenGaussianIsDisabled() {
        Settings settings = Settings.create()
                .set(Keys.GAUSSIAN_ENABLED, false)
                .set(Keys.GAUSSIAN_MIN, 10.0)
                .set(Keys.GAUSSIAN_MAX, 50.0);

        InternalGeneratorContext context = createContext(settings);

        assertThat(context.hasValidGaussianConfiguration())
                .as("Should return false when explicitly disabled by the user")
                .isFalse();
    }

    @Test
    void shouldFallbackToDefaultSdWhenConfiguredSdIsInvalid() {
        Settings settings = Settings.create()
                .set(Keys.GAUSSIAN_SD, -5.0);

        InternalGeneratorContext context = createContext(settings);

        assertThat(context.getGaussianSd())
                .as("Should fallback to the default value of 1.0 when SD is negative or zero")
                .isEqualTo(1.0);
    }

    @Test
    void shouldFallbackToDefaultMeanWhenNotConfigured() {
        Settings settings = Settings.create();

        InternalGeneratorContext context = createContext(settings);

        assertThat(context.getGaussianMean())
                .as("Should fallback to the default value of 0.0 when mean is not set")
                .isEqualTo(0.0);
    }
}
