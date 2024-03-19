package moeba.operator.crossover.generic.rowpermutation.impl;

import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.Random;

import moeba.operator.crossover.generic.rowpermutation.RowPermutationCrossover;

import java.util.Arrays;

/**
 * Implements the Partially Mapped Crossover (PMX) algorithm for row permutation crossovers.
 * PMX is particularly suited for problems where the order and position of elements matter,
 * such as in routing or scheduling problems. It ensures that offspring inherit traits from
 * both parents while maintaining specific element positions.
 */
public class PartiallyMappedCrossover implements RowPermutationCrossover {
    private Random random;

    /**
     * Default constructor initializing a new random number generator.
     */
    public PartiallyMappedCrossover() {
        this.random = new Random();
    }

    /**
     * Constructor allowing for a custom random number generator,
     * useful for testing or specific deterministic operations.
     *
     * @param random Custom random number generator.
     */
    public PartiallyMappedCrossover(Random random) {
        this.random = random;
    }

    /**
     * Executes the PMX crossover on two parent solutions, generating offspring
     * by partially mapping segments of one parent to the other and vice versa.
     *
     * @param s1 The first parent solution.
     * @param s2 The second parent solution.
     */
    @Override
    public void execute(IntegerSolution s1, IntegerSolution s2) {
        int n = s1.variables().size(); // Number of elements in the solution.

        // Select two distinct cutting points randomly within the solution length.
        int cuttingPoint1 = random.nextInt(n);
        int cuttingPoint2 = random.nextInt(n - 1);

        // Ensure cuttingPoint1 is less than cuttingPoint2 and handle edge cases.
        if (cuttingPoint1 == cuttingPoint2) {
            cuttingPoint2 = n - 1;
        } else if (cuttingPoint1 > cuttingPoint2) {
            int swap = cuttingPoint1;
            cuttingPoint1 = cuttingPoint2;
            cuttingPoint2 = swap;
        }

        // Initialize arrays to track replacements between cutting points.
        int[] replacement1 = new int[n];
        int[] replacement2 = new int[n];
        Arrays.fill(replacement1, -1); // Use -1 to indicate no replacement needed.
        Arrays.fill(replacement2, -1);

        int v1, v2;
        // Exchange segments between the cutting points and set up replacements.
        for (int i = cuttingPoint1; i <= cuttingPoint2; i++) {
            v1 = s1.variables().get(i);
            v2 = s2.variables().get(i);

            s1.variables().set(i, v2);
            s2.variables().set(i, v1);

            replacement1[v2] = v1;
            replacement2[v1] = v2;
        }

        // Fill in the remaining slots with appropriate replacements.
        for (int i = 0; i < n; i++) {
            // Outside the segment exchanged, look for replacements.
            if ((i < cuttingPoint1) || (i > cuttingPoint2)) {
                int n1 = s1.variables().get(i);
                int m1 = replacement1[n1];

                int n2 = s2.variables().get(i);
                int m2 = replacement2[n2];

                // Find the final replacement for s1.variables().get(i).
                while (m1 != -1) {
                    n1 = m1;
                    m1 = replacement1[m1];
                }

                // Find the final replacement for s2.variables().get(i).
                while (m2 != -1) {
                    n2 = m2;
                    m2 = replacement2[m2];
                }

                // Set the final replacements into the parent solutions.
                s1.variables().set(i, n1);
                s2.variables().set(i, n2);
            }
        }
    }
}
