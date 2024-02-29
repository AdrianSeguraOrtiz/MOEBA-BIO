package moeba.operator.crossover;

import java.util.List;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

public class GenericCrossover implements CrossoverOperator<IntegerSolution> {
    private double crossoverProbability;
    private int numParents;
	private int numOffspring;   

    public GenericCrossover(double crossoverProbability, int numberOfParents, int numberOfOffspring) {
        this.crossoverProbability = crossoverProbability;
        this.numParents = numberOfParents;
        this.numOffspring = numberOfOffspring;
    }

    @Override
    public List<IntegerSolution> execute(List<IntegerSolution> source) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    @Override
    public int getNumberOfRequiredParents() {
        return numParents;
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return numOffspring;
    }
    
}
