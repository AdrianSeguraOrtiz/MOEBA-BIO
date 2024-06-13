package moeba.parameterization.operator;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.CompositeMutation;

import moeba.parameterization.ParameterizationSolution;

public class ParameterizationMutation implements MutationOperator<ParameterizationSolution> {

    private CompositeMutation compositeMutation;

    public ParameterizationMutation(CompositeMutation compositeMutation) {
        this.compositeMutation = compositeMutation;
    }

    @Override
    public ParameterizationSolution execute(ParameterizationSolution source) {
        return new ParameterizationSolution(compositeMutation.execute(source));
    }

    @Override
    public double getMutationProbability() {
        return compositeMutation.getMutationProbability();
    }

    
    
}
