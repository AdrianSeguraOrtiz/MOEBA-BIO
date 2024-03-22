package moeba.utils.observer;

import moeba.Problem;
import moeba.representationwrapper.RepresentationWrapper;
import moeba.representationwrapper.impl.GenericRepresentationWrapper;
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

    public ProblemObserver(Object[][] data, Class<?>[] types, String[] strFitnessFunctions,
            CacheStorage<String, Double[]> externalCache, CacheStorage<String, Double>[] internalCaches,
            RepresentationWrapper representationWrapper, ObserverInterface[] observers) {

        super(data, types, strFitnessFunctions, externalCache, internalCaches, representationWrapper);
        checkObservers(observers);
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

    /**
     * Checks if the observers passed in the constructor are valid.
     * Throws an IllegalArgumentException if any of the observers require missing dependencies.
     * @param observers the observers to be checked
     */
    public void checkObservers(ObserverInterface[] observers) {
        for (ObserverInterface observer : observers) {
            if (observer instanceof ExternalCacheObserver && super.externalCache == null) {
                throw new IllegalArgumentException("External cache observer requires external cache.");
            }
            if (observer instanceof InternalCacheObserver && super.internalCaches == null) {
                throw new IllegalArgumentException("Internal cache observer requires internal cache.");
            }
            if (observer instanceof BiclusterCountObserver && !(super.representationWrapper instanceof GenericRepresentationWrapper)) {
                throw new IllegalArgumentException("Bicluster count observer requires generic representation wrapper.");
            }
        }
    }
}
