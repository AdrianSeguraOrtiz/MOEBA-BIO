package moeba.operator.crossover;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Random;

import org.mockito.Mockito;

import moeba.operator.crossover.biclustersbinary.BiclusterBinaryCrossover;
import moeba.operator.crossover.biclustersbinary.impl.BicUniformCrossover;
import moeba.operator.crossover.rowpermutation.RowPermutationCrossover;
import moeba.operator.crossover.rowpermutation.impl.CycleCrossover;
import moeba.operator.crossover.rowpermutation.impl.EdgeRecombinationCrossover;
import moeba.operator.crossover.rowpermutation.impl.PartiallyMappedCrossover;
import org.testng.annotations.Test;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

public class CrossoverTest {

    /**
     * Creates an IntegerSolution instance from a given array of values.
     * This utility method is used to setup test scenarios for crossover operations.
     *
     * @param values Array of integer values representing the solution's variables.
     * @return An initialized IntegerSolution object.
     */
    public static IntegerSolution createIntegerSolution(int[] values) {
        List<Bounds<Integer>> integerBounds = new ArrayList<>(values.length);
        // Calculate the minimum and maximum values to define bounds for the solution.
        IntSummaryStatistics stat = Arrays.stream(values).summaryStatistics();
        int min = stat.getMin();
        int max = stat.getMax();
        for (int i = 0; i < values.length; i++) {
            integerBounds.add(Bounds.create(min, max));
        }
        IntegerSolution integerSolution = new DefaultIntegerSolution(1, 0, integerBounds);
        for (int i = 0; i < values.length; i++) {
            integerSolution.variables().set(i, values[i]);
        }
        return integerSolution;
    }

    /**
     * Tests the uniform crossover for bicluster binary solutions.
     * Ensures that offspring solutions match the parents when all bits are set identically,
     * indicating no change during crossover.
     */
    @Test
    public void testUniformCrossover() {
        BitSet parent1 = new BitSet(20);
        BitSet parent2 = new BitSet(20);
        // Initialize parent BitSets with alternating true and false values.
        for (int i = 0; i < parent1.size(); i++) {
            parent1.set(i, i % 2 == 0);
            parent2.set(i, i % 2 == 0);
        }
        
        BiclusterBinaryCrossover crossoverOperator = new BicUniformCrossover();
        BitSet child1 = (BitSet) parent1.clone();
        BitSet child2 = (BitSet) parent2.clone();
        crossoverOperator.execute(child1, child2);
        // Verify that children remain identical to their respective parents post-crossover.
        assertEquals(child1, parent1);
        assertEquals(child2, parent2);
    }

    /**
     * Tests the cycle crossover for row permutation solutions.
     * This test case is based on specific examples and expected outcomes
     * to verify the correctness of the cycle crossover implementation.
     * 
     * NOTE: Based on https://elvex.ugr.es/decsai/computational-intelligence/slides/G2%20Genetic%20Algorithms.pdf
     */
    @Test
    public void testCycleCrossover() {
        // Example scenario and expected outcomes based on cycle crossover characteristics.
        int[] parent1 = new int[]{1,2,3,4,5,6,7,8,9};
        IntegerSolution integerSolutionP1 = createIntegerSolution(parent1);
        int[] parent2 = new int[]{9,3,7,8,2,6,5,1,4};
        IntegerSolution integerSolutionP2 = createIntegerSolution(parent2);

        RowPermutationCrossover crossoverOperator = new CycleCrossover();
        crossoverOperator.execute(integerSolutionP1, integerSolutionP2);
        int[] child1 = integerSolutionP1.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] child2 = integerSolutionP2.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] expectedOffsprint1 = new int[]{1,3,7,4,2,6,5,8,9};
        int[] expectedOffsprint2 = new int[]{9,2,3,8,5,6,7,1,4};
        // Assert that the resulting offspring match one of the expected outcomes.
        assert(Arrays.equals(child1, expectedOffsprint1) || Arrays.equals(child1, expectedOffsprint2));
        assert(Arrays.equals(child2, expectedOffsprint1) || Arrays.equals(child2, expectedOffsprint2));
    }

    /**
     * Tests the partially mapped crossover (PMX) for row permutation solutions.
     * Utilizes mocked random values to control the selection of crossover points,
     * ensuring consistent and predictable outcomes for testing.
     * 
     * NOTE: Based on https://chat.openai.com/share/29e9d113-8b51-422b-8f91-d575cb8d0801
     */
    @Test
    public void testPartiallyMappedCrossover() {
        // Example scenario and expected outcomes based on PMX crossover characteristics.
        int[] parent1 = new int[]{8,4,7,3,6,2,5,1,9,0};
        IntegerSolution integerSolutionP1 = createIntegerSolution(parent1);
        int[] parent2 = new int[]{0,1,2,3,4,5,6,7,8,9};
        IntegerSolution integerSolutionP2 = createIntegerSolution(parent2);

        // Mock random object to control crossover behavior in tests.
        Random mockRandom = Mockito.mock(Random.class);
        Mockito.when(mockRandom.nextInt(anyInt())).thenReturn(6, 3);

        RowPermutationCrossover crossoverOperator = new PartiallyMappedCrossover(mockRandom);
        crossoverOperator.execute(integerSolutionP1, integerSolutionP2);
        int[] child1 = integerSolutionP1.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] child2 = integerSolutionP2.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] expectedOffsprint1 = new int[]{8,2,7,3,4,5,6,1,9,0};
        int[] expectedOffsprint2 = new int[]{0,1,4,3,6,2,5,7,8,9};
        // Assert that the resulting offspring exactly match the expected outcomes.
        assert(Arrays.equals(child1, expectedOffsprint1));
        assert(Arrays.equals(child2, expectedOffsprint2));
    }

    /**
     * Tests the edge recombination crossover for row permutation solutions.
     * This test employs mocked random values to determine starting elements
     * and next element selections, facilitating the validation of crossover outcomes.
     */
    @Test
    public void testEdgeRecombinationCrossover() {
        // Example scenario and expected outcomes based on edge recombination crossover characteristics.
        int[] parent1 = new int[]{1,2,3,4,5};
        IntegerSolution integerSolutionP1 = createIntegerSolution(parent1);
        int[] parent2 = new int[]{5,3,2,4,1};
        IntegerSolution integerSolutionP2 = createIntegerSolution(parent2);

        // Mock random objects to control the selection process within the crossover.
        Random mockRandom1 = Mockito.mock(Random.class);
        Mockito.when(mockRandom1.nextInt(anyInt())).thenReturn(2, 3);
        Random mockRandom2 = Mockito.mock(Random.class);
        Mockito.when(mockRandom2.nextInt(anyInt())).thenReturn(0);

        RowPermutationCrossover crossoverOperator = new EdgeRecombinationCrossover(mockRandom1, mockRandom2);
        crossoverOperator.execute(integerSolutionP1, integerSolutionP2);
        int[] child1 = integerSolutionP1.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] child2 = integerSolutionP2.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] expectedOffsprint1 = new int[]{3,1,2,4,5};
        int[] expectedOffsprint2 = new int[]{4,1,2,3,5};

        // Assert that the resulting offspring match the expected outcomes.
        assert(Arrays.equals(child1, expectedOffsprint1));
        assert(Arrays.equals(child2, expectedOffsprint2));
    }
}