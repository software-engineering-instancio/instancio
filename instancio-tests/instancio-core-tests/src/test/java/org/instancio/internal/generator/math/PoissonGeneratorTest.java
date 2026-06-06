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

class PoissonGeneratorTest {

    @Test
    void shouldThrowExceptionWhenMeanIsZeroOrNegative() {
        // 1. Mock the context and settings
        GeneratorContext context = mock(GeneratorContext.class);
        Settings settings = mock(Settings.class);

        when(context.settings()).thenReturn(settings);
        // Simulate a user passing an invalid negative lambda value
        when(settings.get(Keys.POISSON_LAMBDA)).thenReturn(-1.0);

        PoissonGenerator generator = new PoissonGenerator(context);
        Random random = mock(Random.class);

        // 2. Assert that an IllegalArgumentException is thrown
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(random);
        });

        // 3. Verify the exception message
        assertTrue(exception.getMessage().contains("strictly positive"));
    }
}
