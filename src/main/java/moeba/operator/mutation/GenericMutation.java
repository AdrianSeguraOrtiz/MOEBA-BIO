package moeba.operator.mutation;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

public class GenericMutation implements MutationOperator<CompositeSolution> {
    private double mutationProbability;

    public GenericMutation(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    @Override
    public CompositeSolution execute(CompositeSolution source) {
        // TODO Implement mutation
        return source;
    }

    @Override
    public double getMutationProbability() {
        return mutationProbability;
    }
    
}
