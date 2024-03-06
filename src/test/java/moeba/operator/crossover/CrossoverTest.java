package moeba.operator.crossover;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.BitSet;

import moeba.operator.crossover.biclustersbinary.BiclusterBinaryCrossover;
import moeba.operator.crossover.biclustersbinary.impl.BicUniformCrossover;
import moeba.operator.crossover.rowpermutation.RowPermutationCrossover;
import moeba.operator.crossover.rowpermutation.impl.CycleCrossover;
import org.testng.annotations.Test;

public class CrossoverTest {

    @Test
    public void testUniformCrossover() {
        BitSet parent1 = new BitSet(20);
        BitSet parent2 = new BitSet(20);
        for (int i = 0; i < parent1.size(); i++) {
            parent1.set(i, i % 2 == 0);
            parent2.set(i, i % 2 == 0);
        }
        
        BiclusterBinaryCrossover crossoverOperator = new BicUniformCrossover();
        BitSet child1 = (BitSet) parent1.clone();
        BitSet child2 = (BitSet) parent2.clone();
        crossoverOperator.execute(child1, child2);
        assertEquals(child1, parent1);
        assertEquals(child2, parent2);
    }

    @Test
    public void testCycleCrossover() {
        // Based on https://elvex.ugr.es/decsai/computational-intelligence/slides/G2%20Genetic%20Algorithms.pdf
        int[] parent1 = new int[]{1,2,3,4,5,6,7,8,9};
        int[] parent2 = new int[]{9,3,7,8,2,6,5,1,4};
        RowPermutationCrossover crossoverOperator = new CycleCrossover();
        int[][] result = crossoverOperator.execute(parent1, parent2);
        int[] expectedOffsprint1 = new int[]{1,3,7,4,2,6,5,8,9};
        int[] expectedOffsprint2 = new int[]{9,2,3,8,5,6,7,1,4};
        assert(Arrays.equals(result[0], expectedOffsprint1) || Arrays.equals(result[0], expectedOffsprint2) || Arrays.equals(result[0], parent1) || Arrays.equals(result[0], parent2));
        assert(Arrays.equals(result[1], expectedOffsprint1) || Arrays.equals(result[1], expectedOffsprint2) || Arrays.equals(result[1], parent1) || Arrays.equals(result[1], parent2));
    }
}
