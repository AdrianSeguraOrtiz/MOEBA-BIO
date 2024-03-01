package moeba.utils.fitnessevolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import com.google.common.util.concurrent.AtomicDoubleArray;
import moeba.Problem;

/**
 * This class extends the Problem class to incorporate the functionality of tracking
 * the evolution of fitness values of solutions across generations in an evolutionary
 * optimization process. It is designed to support parallel execution environments by
 * using thread-safe data structures.
 */
public class ProblemFitnessEvolution extends Problem {
    // Thread-safe array to store the best (minimum) fitness values for each objective.
    protected AtomicDoubleArray progressiveValues;

    // Array of lists to track the evolution of the best fitness values for each objective across generations.
    protected ArrayList<Double>[] generationFitness;

    // Counter to manage the number of evaluations, supporting parallel execution tracking.
    protected AtomicInteger parallelCount;

    // The size of the population, crucial for determining when a generation has been fully evaluated.
    protected int populationSize;

    /**
     * Initializes the problem with the given parameters and sets up fitness tracking.
     * This constructor is used when a generic representation of the problem is sufficient.
     *
     * @param data                 The dataset used in the problem.
     * @param types                The data types of each column in the dataset.
     * @param strFitnessFunctions  String representation of the fitness functions to be used.
     * @param populationSize       The size of the population in the evolutionary algorithm.
     */
    public ProblemFitnessEvolution(Object[][] data, Class<?>[] types, String strFitnessFunctions, int populationSize) {
        super(data, types, strFitnessFunctions);
        initializeFitnessTracking(populationSize);
    }

    /**
     * Initializes the problem with the given parameters and sets up fitness tracking.
     * This constructor allows specifying a specific number of biclusters, providing a more tailored problem setup.
     *
     * @param data                 The dataset used in the problem.
     * @param types                The data types of each column in the dataset.
     * @param strFitnessFunctions  String representation of the fitness functions to be used.
     * @param populationSize       The size of the population in the evolutionary algorithm.
     * @param numBiclusters        The number of biclusters to be used in the problem representation.
     */
    public ProblemFitnessEvolution(Object[][] data, Class<?>[] types, String strFitnessFunctions, int populationSize, int numBiclusters) {
        super(data, types, strFitnessFunctions, numBiclusters);
        initializeFitnessTracking(populationSize);
    }

    /**
     * Initializes tracking structures for fitness evolution.
     *
     * @param populationSize The size of the population, used to track generations.
     */
    @SuppressWarnings("unchecked")
    private void initializeFitnessTracking(int populationSize) {
        this.parallelCount = new AtomicInteger();
        this.populationSize = populationSize;
        int numOfObjectives = this.fitnessFunctions.length;
        this.progressiveValues = new AtomicDoubleArray(numOfObjectives);
        this.generationFitness = new ArrayList[numOfObjectives];

        // Initialize arrays to track fitness evolution.
        for (int i = 0; i < numOfObjectives; i++) {
            this.progressiveValues.set(i, Double.MAX_VALUE); // Set high initial values to ensure any first comparison is lower.
            this.generationFitness[i] = new ArrayList<>();
        }
    }

    /**
     * Returns the fitness evolution of each objective function over generations.
     *
     * @return A map with fitness function identifiers as keys and arrays of best fitness values over generations as values.
     */
    public Map<String, Double[]> getFitnessEvolution() {
        Map<String, Double[]> fitnessEvolution = new HashMap<>();
        for (int i = 0; i < fitnessFunctions.length; i++) {
            fitnessEvolution.put("F" + i, this.generationFitness[i].toArray(new Double[0]));
        }
        return fitnessEvolution;
    }

    /**
     * Evaluates the given solution, updating the fitness evolution tracking.
     * Overrides the evaluate method from the Problem class.
     *
     * @param solution The solution to be evaluated.
     * @return The evaluated solution with updated objective values.
     */
    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        IntegerSolution result = super.evaluate(solution);

        // Increment the evaluation count and update the best fitness values if necessary.
        int cnt = parallelCount.incrementAndGet();
        for (int i = 0; i < fitnessFunctions.length; i++) {
            double currentMin = progressiveValues.get(i);
            if (result.objectives()[i] < currentMin) {
                progressiveValues.compareAndSet(i, currentMin, result.objectives()[i]); // Update if current fitness is better.
            }

            // Add the current best fitness to the tracking list at the end of each generation.
            if (cnt % populationSize == 0) {
                generationFitness[i].add(progressiveValues.get(i));
            }
        }

        return result;
    }
}
