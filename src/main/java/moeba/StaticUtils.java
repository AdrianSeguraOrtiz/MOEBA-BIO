package moeba;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

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
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.termination.Termination;
import org.uma.jmetal.util.termination.impl.TerminationByEvaluations;

public final class StaticUtils {

    /**
     * Returns a FitnessFunction object based on a given identifier string
     * @param str identifier string for the fitness function
     * @param data 2D array of data
     * @param types array of data types
     * @param representation the representation used
     * @return a FitnessFunction object
     */
    public static FitnessFunction getFitnessFunctionFromString(String str, Object[][] data, Class<?>[] types) {
        FitnessFunction res;
        switch (str.toLowerCase()) {

            case "biclustersize":
                res = new BiclusterSize(data, types);
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
        public List<IntegerSolution> population;

        /**
         * Constructs an instance of AlgorithmResult.
         * 
         * @param computingTime The total execution time of the algorithm
         * @param population The population at the last iteration of the algorithm
         */
        public AlgorithmResult(long computingTime, List<IntegerSolution> population) {
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
            NaryTournamentSelection<IntegerSolution> selection,
            CrossoverOperator<IntegerSolution> crossover,
            MutationOperator<IntegerSolution> mutation,
            int numThreads) {

        long computingTime;
        List<IntegerSolution> population;

        // Defines the termination condition for the algorithm
        Termination termination = new TerminationByEvaluations(maxEvaluations);
        // Replacement strategy for creating the next generation
        Replacement<IntegerSolution> replacement = new MuPlusLambdaReplacement<>(new ObjectiveComparator<>(0));
        int offspringPopulationSize = populationSize;

        // Executes the algorithm based on the problem's number of objectives and specified algorithm name
        if (problem.getNumberOfObjectives() == 1) {
            // Single-objective problem logic
            if (strAlgorithm.equals("GA-SingleThread")) {
                // Instantiates and executes a single-threaded genetic algorithm
                GeneticAlgorithm<IntegerSolution> algorithm = new GeneticAlgorithm<>(
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

                AsyncMultiThreadGAParents<IntegerSolution> algorithm = new AsyncMultiThreadGAParents<>(
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
                Algorithm<List<IntegerSolution>> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(maxEvaluations)
                        .build();

                AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
                computingTime = algorithmRunner.getComputingTime();
                population = SolutionListUtils.getNonDominatedSolutions(algorithm.getResult());

            } else if (strAlgorithm.equals("NSGAII-AsyncParallel")) {
                // Instantiates and executes an asynchronous parallel NSGA-II algorithm
                long initTime = System.currentTimeMillis();

                AsyncMultiThreadNSGAIIParents<IntegerSolution> algorithm = new AsyncMultiThreadNSGAIIParents<>(
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

                AsyncMultiThreadNSGAIIParentsExternalFile<IntegerSolution> algorithm = new AsyncMultiThreadNSGAIIParentsExternalFile<>(
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
     * Writes the evolution of fitness values to a specified output text file.
     * Each line in the output file corresponds to a series of fitness values
     * for a particular individual or solution, with values separated by commas.
     *
     * @param strFile The path and name of the output file where fitness evolution data will be written.
     * @param fitnessEvolution A map where the key is a string identifier for each individual or solution,
     *                         and the value is an array of Double representing the fitness values over time.
     */
    public static void writeFitnessEvolution(String strFile, Map<String, Double[]> fitnessEvolution) {
        try {
            // Create a File object representing the specified output file.
            File outputFile = new File(strFile);
            // Initialize a BufferedWriter to write text to the output file, wrapping a FileWriter for efficient writing.
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

            // Iterate through each entry in the fitnessEvolution map.
            for (Map.Entry<String, Double[]> entry : fitnessEvolution.entrySet()) {
                // Convert the array of Double fitness values to a string representation, removing the brackets.
                String strVector = Arrays.toString(entry.getValue());
                // Write the string representation of the fitness values array to the file, followed by a newline.
                bw.write(strVector.substring(1, strVector.length() - 1) + "\n");
            }

            // Flush any buffered content to the file.
            bw.flush();
            // Close the BufferedWriter to release system resources.
            bw.close();
        } catch (IOException ioe) {
            // In case of an IOException, wrap and rethrow it as a RuntimeException.
            throw new RuntimeException(ioe);
        }
    }

    public static Integer[][][] getBiclustersFromRepresentation(Integer[] x, Representation representation) {
        return null;
    }
    
}
