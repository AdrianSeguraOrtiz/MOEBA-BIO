package moeba.operator.mutation.generic.rowpermutation;

import org.uma.jmetal.solution.integersolution.IntegerSolution;

public interface RowPermutationMutation {
    
    public void execute(IntegerSolution s, double mutationProbability);

}
