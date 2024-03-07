package moeba.operator.crossover.rowpermutation.impl;

import moeba.operator.crossover.rowpermutation.RowPermutationCrossover;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.Random;

public class CycleCrossover implements RowPermutationCrossover {
    private Random random;

    public CycleCrossover() {
        this.random = new Random();
    }

    public CycleCrossover(Random random) {
        this.random = random;
    }


    @Override
    public void execute(IntegerSolution parent1, IntegerSolution parent2) {
        // Initialize the offspring arrays with the sizes of the parents
        int length = parent1.variables().size();
        
        // Arrays to mark the visited indices for each offspring
        boolean[] visited1 = new boolean[length];
        boolean[] visited2 = new boolean[length];

        // Randomly select a starting index
        int start = random.nextInt(length);
        int index = start;
        
        // Identify and apply the first cycle
        do {
            visited1[index] = true;
            visited2[index] = true;

            // Find the index of parent2[index] in parent1 to continue the cycle
            for (int i = 0; i < length; i++) {
                if (parent1.variables().get(i) == parent2.variables().get(index)) {
                    index = i;
                    break;
                }
            }
        } while (index != start);

        // Fill the gaps with elements from the other parent
        int v1, v2;
        for (int i = 0; i < length; i++) {
            v1 = parent1.variables().get(i);
            v2 = parent2.variables().get(i);
            if (!visited1[i]) {
                parent1.variables().set(i, v2);
            }
            if (!visited2[i]) {
                parent2.variables().set(i, v1);
            }
        }
    }
}
