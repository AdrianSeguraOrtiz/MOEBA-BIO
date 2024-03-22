package moeba.operator.mutation.generic.rowpermutation.impl;

import java.util.Random;

import moeba.operator.mutation.generic.rowpermutation.RowPermutationMutation;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

public class SwapMutation implements RowPermutationMutation {

    private Random random;

    public SwapMutation() {
        this.random = new Random();
    }

    public SwapMutation(Random random) {
        this.random = random;
    }

    @Override
    public void execute(IntegerSolution s, double mutationProbability) {
        int numPositions = s.variables().size();
        int numPositionsChanged = (int)((mutationProbability-0.05 + 0.1*random.nextFloat())*numPositions);
        int index;
        for (int i = 0; i < numPositionsChanged; i++) {
            index = random.nextInt(numPositions);
            int targetIndex = random.nextInt(numPositions);
            int aux = s.variables().get(index);
            s.variables().set(index, s.variables().get(targetIndex));
            s.variables().set(targetIndex, aux);
        }
    }
    
}
