package moeba.parameterization;

import java.util.ArrayList;
import java.util.List;

import moeba.utils.observer.ProblemObserver.ObserverInterface;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

public class ParameterizationSolution extends CompositeSolution {

    public List<List<ParameterizationSolution>> subPopulations;
    public ObserverInterface[] subObservers;

    public ParameterizationSolution(CompositeSolution solution) {
        super(solution);
        this.subPopulations = new ArrayList<>();
    }

    public ParameterizationSolution(List<Solution<?>> solutions) {
        super(solutions);
        this.subPopulations = new ArrayList<>();
    }
    
}
