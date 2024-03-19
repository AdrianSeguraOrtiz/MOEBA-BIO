package moeba.operator.mutation.specific;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

public class SpecificMutation implements MutationOperator<IntegerSolution> {
    private double mutationProbability;

    public SpecificMutation(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    @Override
    public IntegerSolution execute(IntegerSolution source) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public double getMutationProbability() {
        return mutationProbability;
    }
    
}
