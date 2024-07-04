package moeba.parameterization;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

public class ParameterizationSolution extends CompositeSolution {

    public List<List<ParameterizationSolution>> subPopulations;

    public ParameterizationSolution(CompositeSolution solution) {
        super(solution);
        this.subPopulations = new ArrayList<>();
    }

    public ParameterizationSolution(List<Solution<?>> solutions) {
        super(solutions);
        this.subPopulations = new ArrayList<>();
    }
    
}