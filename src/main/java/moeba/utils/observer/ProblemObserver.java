package moeba.utils.observer;

import moeba.Problem;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

/**
 * The ProblemObserver class extends the Problem class to incorporate observer functionality.
 * This class is designed to observe the evolution of solutions in a genetic algorithm or any optimization problem.
 * It allows for registration of observer instances that can perform actions (e.g., logging or writing to a file) when a solution is evaluated.
 */
public class ProblemObserver extends Problem {
    // Array of observer instances to be notified upon solution evaluation
    protected ObserverInterface[] observers;

    /**
     * Defines the contract for observer instances that wish to be notified about solution evaluations.
     */
    public interface ObserverInterface {
        // Method to register a solution evaluation event
        void register(CompositeSolution result);
        // Method to write information to a file
        void writeToFile(String strFile);
    }

    /**
     * Constructor for ProblemObserver with basic setup.
     * @param observers Array of ObserverInterface instances to be notified upon solution evaluations.
     * @param data Problem-specific data required for initialization.
     * @param types Array of Class types related to the problem setup.
     * @param strFitnessFunctions Array of strings representing fitness functions for the problem.
     */
    public ProblemObserver(ObserverInterface[] observers, Object[][] data, Class<?>[] types, String[] strFitnessFunctions) {
        super(data, types, strFitnessFunctions);
        this.observers = observers;
    }

    /**
     * Extended constructor for ProblemObserver including number of biclusters.
     * @param observers Array of ObserverInterface instances to be notified upon solution evaluations.
     * @param data Problem-specific data required for initialization.
     * @param types Array of Class types related to the problem setup.
     * @param strFitnessFunctions Array of strings representing fitness functions for the problem.
     * @param numBiclusters Number of biclusters to be considered, specific to the problem domain.
     */
    public ProblemObserver(ObserverInterface[] observers, Object[][] data, Class<?>[] types, String[] strFitnessFunctions, int numBiclusters) {
        super(data, types, strFitnessFunctions, numBiclusters);
        this.observers = observers;
    }

    /**
     * Overrides the evaluate method from the Problem class to include observer notification logic.
     * @param solution The CompositeSolution instance to be evaluated.
     * @return CompositeSolution The evaluated solution instance, after observer notification.
     */
    @Override
    public CompositeSolution evaluate(CompositeSolution solution) {
        // Call the super class's evaluate method to perform the actual evaluation
        CompositeSolution result = super.evaluate(solution);
        // Notify all registered observers with the evaluation result
        for (ObserverInterface observer : observers) {
            observer.register(result);
        }
        // Return the evaluated solution
        return result;
    }
}
