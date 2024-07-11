package moeba.parameterization;

import java.util.List;
import java.util.ArrayList;

import moeba.utils.observer.ProblemObserver.ObserverInterface;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

public class ParameterizationSolution extends CompositeSolution {

    public List<List<ParameterizationSolution>> subPopulations;
    public List<ObserverInterface[]> subObservers;
    public List<String> tags;

    public ParameterizationSolution(CompositeSolution solution) {
        super(solution);
        this.subPopulations = new ArrayList<>();
        this.subObservers = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public ParameterizationSolution(List<Solution<?>> solutions) {
        super(solutions);
        this.subPopulations = new ArrayList<>();
        this.subObservers = new ArrayList<>();
        this.tags = new ArrayList<>();
    }
    
}
