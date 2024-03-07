package moeba.operator.crossover;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import moeba.operator.crossover.biclustersbinary.BiclusterBinaryCrossover;
import moeba.operator.crossover.biclustersbinary.impl.BicUniformCrossover;
import moeba.operator.crossover.rowpermutation.RowPermutationCrossover;
import moeba.operator.crossover.rowpermutation.impl.CycleCrossover;
import moeba.operator.crossover.rowpermutation.impl.PartiallyMappedCrossover;
import org.testng.annotations.Test;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

public class CrossoverTest {

    public static class RandomMock extends Random {
        public int nextInt(int n) {
            return n==9 ? 6 : 3;
        }
    }

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
        List<Bounds<Integer>> integerBounds = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            integerBounds.add(Bounds.create(1, 9));
        }

        int[] parent1 = new int[]{1,2,3,4,5,6,7,8,9};
        IntegerSolution integerSolutionP1 = new DefaultIntegerSolution(1, 0, integerBounds);
        for (int i = 0; i < 9; i++) {
            integerSolutionP1.variables().set(i, parent1[i]);
        }
      
        int[] parent2 = new int[]{9,3,7,8,2,6,5,1,4};
        IntegerSolution integerSolutionP2 = new DefaultIntegerSolution(1, 0, integerBounds);
        for (int i = 0; i < 9; i++) {
            integerSolutionP2.variables().set(i, parent2[i]);
        }

        RowPermutationCrossover crossoverOperator = new CycleCrossover(new RandomMock());
        crossoverOperator.execute(integerSolutionP1, integerSolutionP2);
        int[] child1 = integerSolutionP1.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] child2 = integerSolutionP2.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] expectedOffsprint1 = new int[]{1,3,7,4,2,6,5,8,9};
        int[] expectedOffsprint2 = new int[]{9,2,3,8,5,6,7,1,4};
        assert(Arrays.equals(child1, expectedOffsprint1) || Arrays.equals(child1, expectedOffsprint2));
        assert(Arrays.equals(child2, expectedOffsprint1) || Arrays.equals(child2, expectedOffsprint2));
    }

    @Test
    public void testPartiallyMappedCrossover() {
        // Based on https://chat.openai.com/share/29e9d113-8b51-422b-8f91-d575cb8d0801
        List<Bounds<Integer>> integerBounds = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            integerBounds.add(Bounds.create(1, 9));
        }

        int[] parent1 = new int[]{8,4,7,3,6,2,5,1,9,0};
        IntegerSolution integerSolutionP1 = new DefaultIntegerSolution(1, 0, integerBounds);
        for (int i = 0; i < 10; i++) {
            integerSolutionP1.variables().set(i, parent1[i]);
        }
      
        int[] parent2 = new int[]{0,1,2,3,4,5,6,7,8,9};
        IntegerSolution integerSolutionP2 = new DefaultIntegerSolution(1, 0, integerBounds);
        for (int i = 0; i < 10; i++) {
            integerSolutionP2.variables().set(i, parent2[i]);
        }

        RowPermutationCrossover crossoverOperator = new PartiallyMappedCrossover(new RandomMock());
        crossoverOperator.execute(integerSolutionP1, integerSolutionP2);
        int[] child1 = integerSolutionP1.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] child2 = integerSolutionP2.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] expectedOffsprint1 = new int[]{8,2,7,3,4,5,6,1,9,0};
        int[] expectedOffsprint2 = new int[]{0,1,4,3,6,2,5,7,8,9};
        assert(Arrays.equals(child1, expectedOffsprint1));
        assert(Arrays.equals(child2, expectedOffsprint2));
    }
}
