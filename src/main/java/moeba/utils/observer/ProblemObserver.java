package moeba.utils.observer;

import moeba.Problem;
import moeba.Representation;
import moeba.utils.observer.impl.BiclusterCountObserver;
import moeba.utils.observer.impl.ExternalCacheObserver;
import moeba.utils.observer.impl.InternalCacheObserver;
import moeba.utils.storage.CacheStorage;
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
     * @param externalCache A cache for storing externally computed values to avoid recalculations.
     * @param internalCaches An array of caches for storing internally computed values, one per fitness function.
     */
    public ProblemObserver(ObserverInterface[] observers, Object[][] data, Class<?>[] types, String[] strFitnessFunctions, CacheStorage<String, Double[]> externalCache, CacheStorage<String, Double>[] internalCaches) {
        super(data, types, strFitnessFunctions, externalCache, internalCaches);
        this.observers = observers;
        checkObservers();
    }

    /**
     * Extended constructor for ProblemObserver including number of biclusters.
     * @param observers Array of ObserverInterface instances to be notified upon solution evaluations.
     * @param data Problem-specific data required for initialization.
     * @param types Array of Class types related to the problem setup.
     * @param strFitnessFunctions Array of strings representing fitness functions for the problem.
     * @param externalCache A cache for storing externally computed values to avoid recalculations.
     * @param internalCaches An array of caches for storing internally computed values, one per fitness function.
     * @param numBiclusters Number of biclusters to be considered, specific to the problem domain.
     */
    public ProblemObserver(ObserverInterface[] observers, Object[][] data, Class<?>[] types, String[] strFitnessFunctions, CacheStorage<String, Double[]> externalCache, CacheStorage<String, Double>[] internalCaches, int numBiclusters) {
        super(data, types, strFitnessFunctions, externalCache, internalCaches, numBiclusters);
        this.observers = observers;
        checkObservers();
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

    /**
     * Checks that the given observers can be used with the current problem settings.
     *
     * @throws IllegalArgumentException if an observer cannot be used with the current problem
     * representation
     */
    public void checkObservers() {
        for (ObserverInterface observer : this.observers) {
            if (super.representation == Representation.SPECIFIC && observer instanceof BiclusterCountObserver) {
                throw new IllegalArgumentException("Specific representation does not support BiclusterSizeObserver.");
            }
            if (observer instanceof ExternalCacheObserver && super.externalCache == null) {
                throw new IllegalArgumentException("External cache observer requires external cache.");
            }
            if (observer instanceof InternalCacheObserver && super.internalCaches == null) {
                throw new IllegalArgumentException("Internal cache observer requires internal cache.");
            }
        }
    }

}
