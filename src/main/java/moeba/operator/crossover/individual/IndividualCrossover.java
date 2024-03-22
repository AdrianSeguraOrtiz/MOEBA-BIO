package moeba.operator.crossover.individual;

import java.util.ArrayList;
import java.util.List;

import moeba.operator.crossover.individual.rowcolbinary.RowColBinaryCrossover;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public class IndividualCrossover implements CrossoverOperator<CompositeSolution> {

    private double crossoverProbability;
    private RowColBinaryCrossover rowColBinaryCrossover;
    private JMetalRandom random;  

    public IndividualCrossover(double crossoverProbability, RowColBinaryCrossover rowColBinaryCrossover) {
        this.crossoverProbability = crossoverProbability;
        this.rowColBinaryCrossover = rowColBinaryCrossover;
        this.random = JMetalRandom.getInstance();
    }

    @Override
    public List<CompositeSolution> execute(List<CompositeSolution> source) {
        Check.notNull(source);
        Check.that(source.size() == 2, "There must be two parents instead of " + source.size());

        List<CompositeSolution> offspring = new ArrayList<>();

        CompositeSolution offSpring1 = (CompositeSolution) source.get(0).copy();
        BinarySet bs1 = (BinarySet) offSpring1.variables().get(1).variables().get(0);

        CompositeSolution offSpring2 = (CompositeSolution) source.get(1).copy();
        BinarySet bs2 = (BinarySet) offSpring2.variables().get(1).variables().get(0);

        if (random.nextDouble(0, 1) <= this.crossoverProbability) {
            rowColBinaryCrossover.execute(bs1, bs2);
        } 

        offspring.add(offSpring1);
        offspring.add(offSpring2);
        return offspring;
    }

    @Override
    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    @Override
    public int getNumberOfRequiredParents() {
        return 2;
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return 2;
    }
    
}
