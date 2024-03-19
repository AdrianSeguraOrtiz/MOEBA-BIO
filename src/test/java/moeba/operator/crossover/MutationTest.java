package moeba.operator.crossover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;

import java.util.ArrayList;
import java.util.Arrays;
import org.uma.jmetal.util.binarySet.BinarySet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Random;

import moeba.operator.mutation.generic.biclusterbinary.impl.BicUniformMutation;
import moeba.operator.mutation.generic.rowpermutation.impl.SwapMutation;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

public class MutationTest {
    
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
     * Tests the uniform mutation for bicluster binary solutions.
     */
    @Test
    public void testUniformMutation() {
        BinarySet bs = new BinarySet(20);
        BinarySet expected = new BinarySet(20);
        expected.set(0);

        Random mockRandom = Mockito.mock(Random.class);
        Mockito.when(mockRandom.nextFloat()).thenReturn(0.4f, 0.6f);
        BicUniformMutation bicUniformMutation = new BicUniformMutation(0.5f, mockRandom);

        bicUniformMutation.execute(bs);
        assertEquals(bs, expected);
    }

    /**
     * Tests the swap mutation for row permutation solutions.
     */
    @Test
    public void testSwapMutation() {
        int[] sol = new int[]{3,6,2,1,5,4,8,7,0,9};
        IntegerSolution s = createIntegerSolution(sol);

        Random mockRandom = Mockito.mock(Random.class);
        Mockito.when(mockRandom.nextFloat()).thenReturn(0.4f, 0.6f);
        Mockito.when(mockRandom.nextInt(anyInt())).thenReturn(6);
        SwapMutation swapMutation = new SwapMutation(0.5f, mockRandom);

        swapMutation.execute(s);
        int[] res = s.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] expected = new int[]{8,6,2,1,5,4,3,7,0,9};
        assert(Arrays.equals(res, expected));
    }
}

