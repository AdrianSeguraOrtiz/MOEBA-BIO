package moeba.operator.crossover.generic.rowpermutation.impl;

import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.HashMap;
import java.util.Map;

import moeba.operator.crossover.generic.rowpermutation.RowPermutationCrossover;

/**
 * Implements the cycle crossover algorithm for row permutation crossovers.
 * This crossover strategy is used for integer solutions, where the goal is
 * to exchange sequences of values (cycles) between two parent solutions without
 * disrupting the order of values.
 */
public class CycleCrossover implements RowPermutationCrossover {

    /**
     * Executes the cycle crossover between two parent solutions.
     * The crossover identifies cycles between the parents and exchanges these cycles
     * to produce new offspring solutions. It ensures that each value from the parent solutions
     * is present in the offspring without repetition and that the order of values is preserved.
     *
     * @param parent1 The first parent solution.
     * @param parent2 The second parent solution.
     */
    @Override
    public void execute(IntegerSolution parent1, IntegerSolution parent2) {
        int length = parent1.variables().size(); // The length of the solution.
    
        // Create value-to-index maps for both parents to efficiently find positions.
        Map<Integer, Integer> indexMapParent1 = new HashMap<>(length);
        Map<Integer, Integer> indexMapParent2 = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            indexMapParent1.put(parent1.variables().get(i), i);
            indexMapParent2.put(parent2.variables().get(i), i);
        }

        boolean[] visited = new boolean[length]; // Tracks visited positions to form cycles.

        // Iterate over each position to find and process cycles.
        for (int start = 0; start < length; start++) {
            if (!visited[start]) {
                int index = start;
                // Traverse the cycle starting at 'start' index.
                do {
                    visited[index] = true;
                    int itemInParent2 = parent2.variables().get(index);
                    index = indexMapParent1.get(itemInParent2); // Find the next index in the cycle.
                } while (index != start);

                // Exchange values between the parents based on the identified cycle.
                for (int i = 0; i < length; i++) {
                    if (visited[i]) {
                        int temp = parent1.variables().get(i);
                        parent1.variables().set(i, parent2.variables().get(i));
                        parent2.variables().set(i, temp);
                    }
                }
            }
        }
    }
}
