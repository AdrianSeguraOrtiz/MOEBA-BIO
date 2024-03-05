package moeba.operator.crossover.rowpermutation.impl;

import moeba.operator.crossover.rowpermutation.RowPermutationCrossover;
import java.util.Random;

public class CycleCrossover implements RowPermutationCrossover {
    private Random random;

    public CycleCrossover() {
        this.random = new Random();
    }

    /**
     * Executes the crossover operation to create offspring from two parent arrays.
     * 
     * @param parent1 the first parent array
     * @param parent2 the second parent array
     * @return the offspring array containing the children of the two parents
     */
    @Override
    public int[][] execute(int[] parent1, int[] parent2) {
        // Initialize the offspring arrays with the sizes of the parents
        int length = parent1.length;
        int[][] offspring = new int[2][length];
        
        // Arrays to mark the visited indices for each offspring
        boolean[] visited1 = new boolean[length];
        boolean[] visited2 = new boolean[length];
        
        // Initialize the offspring arrays with -1 or any invalid value
        for (int i = 0; i < length; i++) {
            offspring[0][i] = -1;
            offspring[1][i] = -1;
        }

        // Randomly select a starting index
        int start = random.nextInt(length);
        int index = start;
        
        // Identify and apply the first cycle
        do {
            offspring[0][index] = parent1[index];
            offspring[1][index] = parent2[index];
            visited1[index] = true;
            visited2[index] = true;

            // Find the index of parent2[index] in parent1 to continue the cycle
            for (int i = 0; i < length; i++) {
                if (parent1[i] == parent2[index]) {
                    index = i;
                    break;
                }
            }
        } while (index != start);

        // Fill the gaps with elements from the other parent
        for (int i = 0; i < length; i++) {
            if (!visited1[i]) {
                offspring[0][i] = parent2[i];
            }
            if (!visited2[i]) {
                offspring[1][i] = parent1[i];
            }
        }

        return offspring;
    }
}
