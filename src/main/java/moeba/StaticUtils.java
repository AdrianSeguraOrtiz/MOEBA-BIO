package moeba;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;

import moeba.algorithm.AsyncMultiThreadGAParents;
import moeba.algorithm.AsyncMultiThreadNSGAIIParents;
import moeba.algorithm.AsyncMultiThreadNSGAIIParentsExternalFile;
import moeba.fitnessfunction.FitnessFunction;
import moeba.fitnessfunction.impl.BiclusterSize;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.experimental.componentbasedalgorithm.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithm;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.Replacement;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.impl.MuPlusLambdaReplacement;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.impl.NaryTournamentSelection;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.termination.Termination;
import org.uma.jmetal.util.termination.impl.TerminationByEvaluations;

public final class StaticUtils {

    /**
     * Returns a FitnessFunction object based on a given identifier string
     * @param str identifier string for the fitness function
     * @param data 2D array of data
     * @param types array of data types
     * @param cache the internal cache of the fitness function
     * @return a FitnessFunction object
     */
    public static FitnessFunction getFitnessFunctionFromString(String str, Object[][] data, Class<?>[] types, Map<String, Double> cache) {
        FitnessFunction res;
        switch (str.toLowerCase()) {

            case "biclustersize":
                res = new BiclusterSize(data, types, cache);
                break;
            /**
            case "biclustervariance":
                res = new BiclusterVariance(data, types);
                break;
            case "biclusterrowvariance":
                res = new BiclusterRowVariance(data, types);
                break;
            case "meansquaredresidue":
                res = new MeanSquaredResidue(data, types);
                break;
            case "scalingmeansquaredresidue":
                res = new ScalingMeanSquaredResidue(data, types);
                break;
            case "averagecorrelationfunction":
                res = new AverageCorrelationFunction(data, types);
                break;
            case "averagecorrelationvalue":
                res = new AverageCorrelationValue(data, types);
                break;
            case "virtualerror":
                res = new VirtualError(data, types);
                break;
            case "coefficientofvariationfunction":
                res = new CoefficientOfVariationFunction(data, types);
                break;
            */
            default:
                throw new RuntimeException("The fitness function " + str + " is not implemented.");
        }

        return res;
    }

    /**
     * Converts a CSV file to a bidimensional array of objects.
     * 
     * @param inputDataset The input CSV file to read
     * @return A bidimensional array of objects, where each row corresponds to a line in the CSV file and each column
     *         corresponds to a value in that line.
     * @throws IOException If there is an error reading the input CSV file
     */
    public static Object[][] csvToObjectMatrix(File inputDataset) throws IOException {
        // Read all lines from the CSV file sequentially
        List<String> lines = Files.readAllLines(inputDataset.toPath());
        
        // Process lines in parallel to convert them to an array of objects
        Object[][] matrix = lines.parallelStream()
            .skip(1) // Ignore the first line as it contains the heading
            .map(line -> line.split(",")) // Assumes comma as CSV separator
            .map(array -> (Object[]) array) // Direct conversion of String[] to Object[]
            .collect(Collectors.toList()) // Collect results in a list
            .toArray(new Object[0][]); // Convert list to a bidimensional array of objects
        
        return matrix;
    }

    /**
     * Converts a JSON file to an array of Class objects based on the provided column names.
     * 
     * @param inputJsonFile The input JSON file to read column types from
     * @param columnNames The names of the columns to map to Class objects
     * @return An array of Class objects corresponding to the column names
     * @throws IOException If there is an error reading the input JSON file
     * @throws IllegalArgumentException If a column name is not found in the JSON file or if an unsupported type is encountered
     */
    public static Class<?>[] jsonToClassArray(File inputJsonFile, String[] columnNames) throws IOException, IllegalArgumentException {
        // Read column types from the input JSON file
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> columnTypes = objectMapper.readValue(inputJsonFile, new TypeReference<Map<String, String>>() {});

        // Define mapping of column type strings to Class objects
        Map<String, Class<?>> typeMapping = new HashMap<>();
        typeMapping.put("string", String.class);
        typeMapping.put("int", Integer.class);
        typeMapping.put("double", Double.class);
        typeMapping.put("float", Float.class);
        typeMapping.put("boolean", Boolean.class);

        // Initialize an array to hold the Class objects for the columns
        Class<?>[] columnClasses = new Class<?>[columnNames.length];

        // Map column types to Class objects based on the provided column names
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            String type = columnTypes.get(columnName);
            if (type == null) {
                throw new IllegalArgumentException("Column '" + columnName + "' not found in the JSON file.");
            }
            
            Class<?> columnClass = typeMapping.get(type.toLowerCase());
            if (columnClass != null) {
                columnClasses[i] = columnClass;
            } else {
                throw new IllegalArgumentException("Unsupported type: " + type);
            }
        }

        return columnClasses;
    }

    /**
     * Represents the result of an evolutionary algorithm execution.
     */
    public static class AlgorithmResult {
        // Execution time of the algorithm
        public long computingTime;
        // Population at the last iteration of the algorithm
        public List<CompositeSolution> population;

        /**
         * Constructs an instance of AlgorithmResult.
         * 
         * @param computingTime The total execution time of the algorithm
         * @param population The population at the last iteration of the algorithm
         */
        public AlgorithmResult(long computingTime, List<CompositeSolution> population) {
            this.computingTime = computingTime;
            this.population = population;
        }
    }

    /**
     * Executes an evolutionary algorithm based on the specified parameters.
     * 
     * @param problem The problem to be solved by the evolutionary algorithm
     * @param populationSize The size of the population
     * @param maxEvaluations The maximum number of evaluations
     * @param strAlgorithm The name of the algorithm to execute
     * @param selection The selection operator
     * @param crossover The crossover operator
     * @param mutation The mutation operator
     * @param numThreads The number of threads to use for parallel execution
     * @return An AlgorithmResult containing the total execution time and the final population
     * @throws IllegalArgumentException If the specified algorithm is not supported for the problem type
     */
    public static AlgorithmResult executeEvolutionaryAlgorithm(
            Problem problem,
            int populationSize,
            int maxEvaluations,
            String strAlgorithm,
            NaryTournamentSelection<CompositeSolution> selection,
            CrossoverOperator<CompositeSolution> crossover,
            MutationOperator<CompositeSolution> mutation,
            int numThreads) {

        long computingTime;
        List<CompositeSolution> population;

        // Defines the termination condition for the algorithm
        Termination termination = new TerminationByEvaluations(maxEvaluations);
        // Replacement strategy for creating the next generation
        Replacement<CompositeSolution> replacement = new MuPlusLambdaReplacement<>(new ObjectiveComparator<>(0));
        int offspringPopulationSize = populationSize;

        // Executes the algorithm based on the problem's number of objectives and specified algorithm name
        if (problem.getNumberOfObjectives() == 1) {
            // Single-objective problem logic
            if (strAlgorithm.equals("GA-SingleThread")) {
                // Instantiates and executes a single-threaded genetic algorithm
                GeneticAlgorithm<CompositeSolution> algorithm = new GeneticAlgorithm<>(
                        problem,
                        populationSize,
                        offspringPopulationSize,
                        selection,
                        crossover,
                        mutation,
                        termination);

                algorithm.run();
                computingTime = algorithm.getTotalComputingTime();
                population = SolutionListUtils.getNonDominatedSolutions(algorithm.getResult());

            } else if (strAlgorithm.equals("GA-AsyncParallel")) {
                // Instantiates and executes an asynchronous parallel genetic algorithm
                long initTime = System.currentTimeMillis();

                AsyncMultiThreadGAParents<CompositeSolution> algorithm = new AsyncMultiThreadGAParents<>(
                        numThreads,
                        problem,
                        populationSize,
                        crossover,
                        mutation,
                        selection,
                        replacement,
                        termination);

                algorithm.run();
                long endTime = System.currentTimeMillis();
                computingTime = endTime - initTime;
                population = SolutionListUtils.getNonDominatedSolutions(algorithm.getResult());

            } else {
                throw new IllegalArgumentException("The algorithm " + strAlgorithm + " is not available for single-objective problems.");
            }
        } else {
            // Multi-objective problem logic
            if (strAlgorithm.equals("NSGAII-SingleThread")) {
                // Instantiates and executes a single-threaded NSGA-II algorithm
                Algorithm<List<CompositeSolution>> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(maxEvaluations)
                        .build();

                AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
                computingTime = algorithmRunner.getComputingTime();
                population = SolutionListUtils.getNonDominatedSolutions(algorithm.getResult());

            } else if (strAlgorithm.equals("NSGAII-AsyncParallel")) {
                // Instantiates and executes an asynchronous parallel NSGA-II algorithm
                long initTime = System.currentTimeMillis();

                AsyncMultiThreadNSGAIIParents<CompositeSolution> algorithm = new AsyncMultiThreadNSGAIIParents<>(
                        numThreads,
                        problem,
                        populationSize,
                        crossover,
                        mutation,
                        termination);

                algorithm.run();
                long endTime = System.currentTimeMillis();
                computingTime = endTime - initTime;
                population = SolutionListUtils.getNonDominatedSolutions(algorithm.getResult());

            } else if (strAlgorithm.equals("NSGAIIExternalFile-AsyncParallel")) {
                // Instantiates and executes an asynchronous parallel NSGA-II algorithm with external file support
                long initTime = System.currentTimeMillis();

                AsyncMultiThreadNSGAIIParentsExternalFile<CompositeSolution> algorithm = new AsyncMultiThreadNSGAIIParentsExternalFile<>(
                        numThreads,
                        problem,
                        populationSize,
                        crossover,
                        mutation,
                        termination);

                algorithm.run();
                long endTime = System.currentTimeMillis();
                computingTime = endTime - initTime;
                population = SolutionListUtils.getNonDominatedSolutions(algorithm.getResult());

            } else {
                throw new IllegalArgumentException("The algorithm " + strAlgorithm + " is not available for multi-objective problems.");
            }
        }

        return new AlgorithmResult(computingTime, population);
    }

    /**
     * Extracts biclusters from the given composite solution and representation.
     * 
     * @param solution the composite solution
     * @param representation the representation type
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @return a list of biclusters represented as ArrayList of ArrayList of Integers
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<ArrayList<Integer>[]> getBiclustersFromRepresentation(CompositeSolution solution, Representation representation, int numRows, int numCols) {
        
        // Initialize the result list
        ArrayList<ArrayList<Integer>[]> res = new ArrayList<>();
        
        // Extract integer and binary variables from the composite solution
        List<Integer> integerVariables = ((IntegerSolution) solution.variables().get(0)).variables();
        List<BinarySet> binaryVariables = ((BinarySolution) solution.variables().get(1)).variables();
        
        // Check if the representation is generic
        if (representation == Representation.GENERIC) {
            
            // Initialize rows, cols, and precalculatedSums
            ArrayList<Integer> rows = new ArrayList<>();
            ArrayList<Integer> cols = new ArrayList<>();
            int[][] precalculatedSums = new int[numCols][numRows + 1];
            
            // Calculate precalculatedSums
            for (int j = 0; j < numCols; j++) {
                precalculatedSums[j][0] = 0;
                for (int i = 1; i <= numRows; i++) {
                    precalculatedSums[j][i] = precalculatedSums[j][i - 1] + (binaryVariables.get(j+1).get(integerVariables.get(i-1)) ? 1 : 0);
                }
            }

            // Extract biclusters
            for (int i = 0; i < numRows; i++) {
                rows.add(integerVariables.get(i));
                if (binaryVariables.get(0).get(i) || i == numRows - 1) {
                    for (int j = 0; j < numCols; j++) {
                        if (((float) (precalculatedSums[j][i + 1] - precalculatedSums[j][i - rows.size() + 1]) / rows.size()) > 0.5) {
                            cols.add(j);
                        }
                    }
                    
                    // Create and add bicluster to the result list
                    ArrayList<Integer>[] bicluster = new ArrayList[2];
                    Collections.sort(rows);
                    bicluster[0] = new ArrayList<>(rows);
                    Collections.sort(cols);
                    bicluster[1] = new ArrayList<>(cols);
                    res.add(bicluster);

                    // Clear rows and cols for next iteration
                    rows.clear();
                    cols.clear();
                }
            }
            return res;
        } else {
            // TODO: Implement specific representation
            return null;
        }
    }
    
}
