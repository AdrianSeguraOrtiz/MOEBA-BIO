package moeba;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.ConcurrentHashMap;

import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import moeba.fitnessfunction.FitnessFunction;
import moeba.problem.AbstractMixedIntegerBinaryProblem;

/**
 * Extends AbstractMixedIntegerBinaryProblem to define a custom problem with both integer and binary solution components.
 * It supports two types of representations: GENERIC and SPECIFIC, for addressing different problem structures.
 */
public class Problem extends AbstractMixedIntegerBinaryProblem {

    private Object [][] data;
    private Class<?> [] types;
    private FitnessFunction[] fitnessFunctions;
    protected Representation representation;
    protected ConcurrentHashMap<String, Double[]> externalCache;
    protected ConcurrentHashMap<String, Double>[] internalCaches;
    private EvaluateFunction evaluateFunction;

    public interface EvaluateFunction {
        public CompositeSolution evaluate(CompositeSolution solution, ArrayList<ArrayList<Integer>[]> biclusters);
    }

    /**
     * Constructor for the generic representation of the problem.
     * Initializes the problem with generic settings suitable for a wide range of optimization problems.
     *
     * @param data The dataset used in the problem.
     * @param types The data types of each column in the dataset.
     * @param strFitnessFunctions The string identifiers of the fitness functions to be used.
     * @param externalCache A cache for storing externally computed values to avoid recalculations.
     * @param internalCaches An array of caches for storing internally computed values, one per fitness function.
     */
    public Problem(Object[][] data, Class<?> [] types, String[] strFitnessFunctions, ConcurrentHashMap<String, Double[]> externalCache, ConcurrentHashMap<String, Double>[] internalCaches) {
        super(data.length, 1 + data[0].length, 0, data.length - 1, data.length);
        representation = Representation.GENERIC;
        initialize(data, types, strFitnessFunctions, externalCache, internalCaches, -1);
    }

    /**
     * Constructor for the specific representation of the problem.
     * This is more tailored and requires the number of biclusters as a parameter, offering a more customized approach.
     *
     * @param data The dataset used in the problem.
     * @param types The data types of each column in the dataset.
     * @param strFitnessFunctions The string identifiers of the fitness functions to be used.
     * @param externalCache A cache for storing externally computed values to avoid recalculations.
     * @param internalCaches An array of caches for storing internally computed values, one per fitness function.
     * @param numBiclusters The number of biclusters to be considered in the problem.
     */
    public Problem(Object[][] data, Class<?> [] types, String[] strFitnessFunctions, ConcurrentHashMap<String, Double[]> externalCache, ConcurrentHashMap<String, Double>[] internalCaches, int numBiclusters) {
        super(data.length, numBiclusters, 0, numBiclusters - 1, data[0].length);
        if (numBiclusters < 2 || numBiclusters >= data.length) {
            throw new IllegalArgumentException("The number of biclusters must be between 2 and " + (data.length - 1) + ".");
        }
        representation = Representation.SPECIFIC;
        initialize(data, types, strFitnessFunctions, externalCache, internalCaches, numBiclusters);
    }

    /**
     * Creates a solution for the problem, which is a combination of integer and binary solutions.
     * The method depends on the problem's representation (GENERIC or SPECIFIC).
     *
     * @return CompositeSolution The composite solution including both integer and binary parts.
     */
    @Override
    public CompositeSolution createSolution() {
        IntegerSolution integerSolution = new DefaultIntegerSolution(getNumberOfObjectives(), getNumberOfConstraints(), super.integerBounds);
        BinarySolution binarySolution = new DefaultBinarySolution(super.numBitsPerVariable, getNumberOfObjectives());

        if (representation == Representation.GENERIC) {
            // Ensure that the initial number of clusters is varied within an acceptable range
            Random random = new Random();
            binarySolution.variables().get(0).clear();
            float limit = random.nextFloat()/5 + 0.05f;

            // Ensure that the integer part is a permutation
            List<Integer> rowIndexes = IntStream.rangeClosed(0, data.length - 1).boxed().collect(Collectors.toList());
            Collections.shuffle(rowIndexes);

            // Take advantage of the loop to perform both operations simultaneously
            for (int i = 0; i < data.length; i++) {
                integerSolution.variables().set(i, rowIndexes.get(i));
                if (random.nextFloat() < limit) {
                    binarySolution.variables().get(0).set(i);
                }
            }
        }

        return new CompositeSolution(Arrays.asList(integerSolution, binarySolution));
    }
    
    /**
     * Evaluates a solution, updating its objectives based on the defined fitness functions.
     * This involves converting the solution representation to biclusters and applying the fitness functions.
     *
     * @param solution The CompositeSolution instance to be evaluated.
     * @return CompositeSolution The evaluated solution with updated objective values.
     */
    @Override
    public CompositeSolution evaluate(CompositeSolution solution) {
        ArrayList<ArrayList<Integer>[]> biclusters = StaticUtils.getBiclustersFromRepresentation(solution, representation, data.length, data[0].length);
        return evaluateFunction.evaluate(solution, biclusters);
    }

    /**
     * Initializes the problem by setting up the data, types, and fitness functions.
     * This method is common for both constructors and handles the shared initialization logic.
     *
     * @param data The dataset used in the problem.
     * @param types The data types of each column in the dataset.
     * @param strFitnessFunctions The string identifiers of the fitness functions to be used.
     * @param externalCache A cache for storing externally computed values to avoid recalculations.
     * @param internalCaches An array of caches for storing internally computed values, one per fitness function.
     * @param numBiclusters The number of biclusters, relevant for specific representation.
     */
    private void initialize(Object[][] data, Class<?> [] types, String[] strFitnessFunctions, ConcurrentHashMap<String, Double[]> externalCache, ConcurrentHashMap<String, Double>[] internalCaches, int numBiclusters) {
        this.data = data;
        this.types = types;
        this.externalCache = externalCache;
        this.internalCaches = internalCaches;
        this.evaluateFunction = externalCache == null ? this::evaluateWithoutCache : this::evaluateWithCache;
        
        // Initialize fitness functions based on provided string identifiers
        this.fitnessFunctions = new FitnessFunction[strFitnessFunctions.length];
        for (int i = 0; i < strFitnessFunctions.length; i++) {
            this.fitnessFunctions[i] = StaticUtils.getFitnessFunctionFromString(strFitnessFunctions[i], data, types, internalCaches == null ? null : internalCaches[i]);
        }

        // Configure the problem's parameters based on the representation
        int numRows = data.length;
        setNumberOfVariables((representation == Representation.GENERIC) ? (numRows + 1 + data[0].length) : (numRows + numBiclusters));
        setNumberOfObjectives(this.fitnessFunctions.length);
        setName("Problem");
    }

    /**
     * Evaluates the solution without using the cache, directly applying the fitness functions.
     *
     * @param solution The CompositeSolution instance to be evaluated.
     * @param biclusters The biclusters obtained from the solution representation.
     * @return CompositeSolution The evaluated solution with updated objective values.
     */
    public CompositeSolution evaluateWithoutCache(CompositeSolution solution, ArrayList<ArrayList<Integer>[]> biclusters){
        // Apply each fitness function to the biclusters and update the solution objectives
        for (int i = 0; i < fitnessFunctions.length; i++){
            solution.objectives()[i] = fitnessFunctions[i].run(biclusters);
        }
        return solution;
    }

    /**
     * Evaluates the solution using the external cache to avoid recalculating known results.
     * 
     * @param solution The solution to evaluate.
     * @param biclusters The biclusters derived from the solution.
     * @return The evaluated solution with updated objectives, potentially leveraging cached values.
     */
    public CompositeSolution evaluateWithCache(CompositeSolution solution, ArrayList<ArrayList<Integer>[]> biclusters){
        String key = StaticUtils.biclustersToString(biclusters);
        if (externalCache.containsKey(key)){
            for (int i = 0; i < fitnessFunctions.length; i++){
                solution.objectives()[i] = externalCache.get(key)[i];
            }
        } else {
            solution = evaluateWithoutCache(solution, biclusters);
            Double[] scores = new Double[fitnessFunctions.length];
            for (int i = 0; i < fitnessFunctions.length; i++){
                scores[i] = solution.objectives()[i];
            }
            externalCache.put(key, scores);
        }

        return solution;
    }

}
