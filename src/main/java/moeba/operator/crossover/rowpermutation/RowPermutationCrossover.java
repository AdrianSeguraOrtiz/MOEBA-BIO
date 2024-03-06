package moeba.operator.crossover.rowpermutation;

import org.uma.jmetal.solution.integersolution.IntegerSolution;

public interface RowPermutationCrossover {

    public void execute(IntegerSolution s1, IntegerSolution s2);
}
