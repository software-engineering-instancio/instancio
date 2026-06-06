package org.instancio.internal.generator.math;

import org.instancio.Random;
import org.instancio.generator.GeneratorContext;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BinomialGeneratorTest {

    @Test
    void shouldThrowExceptionWhenParametersAreInvalid() {
        // 1. Mock the context and settings
        GeneratorContext context = mock(GeneratorContext.class);
        Settings settings = mock(Settings.class);

        when(context.settings()).thenReturn(settings);
        // Simulate a user passing invalid trials and probability
        when(settings.get(Keys.BINOMIAL_TRIALS)).thenReturn(-5);
        when(settings.get(Keys.BINOMIAL_PROBABILITY)).thenReturn(1.5);

        BinomialGenerator generator = new BinomialGenerator(context);
        Random random = mock(Random.class);

        // 2. Assert that an IllegalArgumentException is thrown
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(random);
        });

        // 3. Verify the exception message
        assertTrue(exception.getMessage().contains("between 0.0 and 1.0"));
    }
}