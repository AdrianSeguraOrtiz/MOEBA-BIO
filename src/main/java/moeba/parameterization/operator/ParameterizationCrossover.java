package moeba.parameterization.operator;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.CompositeCrossover;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

import moeba.parameterization.ParameterizationSolution;

public class ParameterizationCrossover implements CrossoverOperator<ParameterizationSolution> {

    private CompositeCrossover compositeCrossover;

    public ParameterizationCrossover(CompositeCrossover compositeCrossover) {
        this.compositeCrossover = compositeCrossover;
    }

    @Override
    public List<ParameterizationSolution> execute(List<ParameterizationSolution> source) {
        List<CompositeSolution> newSource = new ArrayList<>();
        newSource.add(source.get(0));
        newSource.add(source.get(1));
        List<CompositeSolution> oldOffsprint = compositeCrossover.execute(newSource);
        List<ParameterizationSolution> offsprint = new ArrayList<>();
        offsprint.add(new ParameterizationSolution(oldOffsprint.get(0)));
        offsprint.add(new ParameterizationSolution(oldOffsprint.get(1)));
        return offsprint;
    }

    @Override
    public double getCrossoverProbability() {
        return compositeCrossover.getCrossoverProbability();
    }

    @Override
    public int getNumberOfRequiredParents() {
        return compositeCrossover.getNumberOfRequiredParents();
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return compositeCrossover.getNumberOfGeneratedChildren();
    }
    
}
