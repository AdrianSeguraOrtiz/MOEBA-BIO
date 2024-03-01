package moeba.operator.crossover;

import java.util.List;
import java.util.ArrayList;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public class GenericCrossover implements CrossoverOperator<IntegerSolution> {
    private double crossoverProbability;
    private int numParents;
	private int numOffspring;
    private JMetalRandom random;  

    public GenericCrossover(double crossoverProbability, int numberOfParents, int numberOfOffspring) {
        this.crossoverProbability = crossoverProbability;
        this.numParents = numberOfParents;
        this.numOffspring = numberOfOffspring;
        this.random = JMetalRandom.getInstance();
    }

    @Override
    public List<IntegerSolution> execute(List<IntegerSolution> source) {
        List<IntegerSolution> offspring = new ArrayList<>();
        if (random.nextDouble(0, 1) <= this.crossoverProbability) {
            // TODO: Implement crossover
            for (int k = 0; k < numOffspring; k++) {
                offspring.add((IntegerSolution) source.get(k).copy());
            }
        } else {
            for (int k = 0; k < numOffspring; k++) {
                offspring.add((IntegerSolution) source.get(k).copy());
            }
        }
        return offspring;
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
