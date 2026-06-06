package org.instancio;

import org.instancio.settings.Keys;
import org.instancio.settings.Settings;

import java.util.List;

public class GaussianDemonstration {

    public static void main(String[] args) {
        System.out.println("--- Instancio Truncated Gaussian Generator Demonstration ---");
        
        // 1. Define the Gaussian parameters
        Settings gaussianSettings = Settings.create()
                .set(Keys.GAUSSIAN_ENABLED, true)
                .set(Keys.GAUSSIAN_MEAN, 50.0)
                .set(Keys.GAUSSIAN_SD, 10.0)
                .set(Keys.GAUSSIAN_MIN, 20.0)
                .set(Keys.GAUSSIAN_MAX, 80.0);

        System.out.println("Configuration:");
        System.out.println("Mean (Target): 50.0 | Standard Deviation: 10.0 | Min: 20.0 | Max: 80.0\n");

        // 2. Generate a small sample to show actual output visually
        System.out.println("Generated Sample (15 values):");
        List<Double> visualSample = Instancio.ofList(Double.class)
                .size(15)
                .withSettings(gaussianSettings)
                .create();

        for (int i = 0; i < visualSample.size(); i++) {
            System.out.printf("Value %2d: %.4f%n", (i + 1), visualSample.get(i));
        }

        // 3. Generate a massive dataset to prove statistical accuracy
        System.out.println("\n--- Statistical Proof ---");
        System.out.println("Generating 100,000 values to calculate the true average...");
        
        List<Double> massiveDataset = Instancio.ofList(Double.class)
                .size(100000)
                .withSettings(gaussianSettings)
                .create();

        double sum = 0;
        int outOfBoundsCount = 0;

        for (Double val : massiveDataset) {
            sum += val;
            if (val < 20.0 || val > 80.0) {
                outOfBoundsCount++;
            }
        }

        double calculatedAverage = sum / massiveDataset.size();

        System.out.printf("Total values generated: %d%n", massiveDataset.size());
        System.out.printf("Values outside MIN/MAX bounds: %d%n", outOfBoundsCount);
        System.out.printf("Calculated Average: %.4f (Should be very close to Mean 50.0)%n", calculatedAverage);
        System.out.println("----------------------------------------------------------");
    }
}