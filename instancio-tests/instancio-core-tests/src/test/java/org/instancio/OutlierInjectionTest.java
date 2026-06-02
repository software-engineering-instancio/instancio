package org.instancio;

import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutlierInjectionTest {

    @Test
    void shouldInjectOutlierWhenProbabilityIsHundredPercent() {
        // 1. Settings: 100% anomaly probability and a massive severity multiplier (10,000.0)
        Settings settings = Settings.create()
                .set(Keys.OUTLIER_PROBABILITY, 1.0)
                .set(Keys.OUTLIER_SEVERITY, 10000.0);

        // 2. No overrides! Calling your built-in core generator directly
        Integer result = Instancio.of(Integer.class)
                .withSettings(settings)
                .create();

        // 3. TEST ASSERTION: The severity is very high, so the number must be extremely large (or small)
        assertThat(Math.abs((long) result)).isGreaterThanOrEqualTo(10000L);
    }

    @Test
    void shouldTestZeroSpikeLogicWhenBaseIsSmall() {
        // 1. Settings: 100% anomaly probability and a severity of 500.0
        Settings settings = Settings.create()
                .set(Keys.OUTLIER_PROBABILITY, 1.0)
                .set(Keys.OUTLIER_SEVERITY, 500.0);

        // 2. Using Double to avoid hitting strict primitive type boundaries
        Double result = Instancio.of(Double.class)
                .withSettings(settings)
                .create();

        // 3. TEST ASSERTION: Even if it generates a 0, your "zero spike" logic 
        // will ensure a much larger value is assigned (e.g., 500 * 100).
        assertThat(Math.abs(result)).isGreaterThanOrEqualTo(500.0);
    }
}
