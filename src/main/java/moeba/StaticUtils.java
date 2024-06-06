package moeba;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import moeba.algorithm.AsyncMultiThreadGAParents;
import moeba.algorithm.AsyncMultiThreadNSGAIIParents;
import moeba.algorithm.AsyncMultiThreadNSGAIIParentsExternalFile;
import moeba.fitnessfunction.FitnessFunction;
import moeba.fitnessfunction.impl.BiclusterSizeNormComp;
import moeba.fitnessfunction.impl.BiclusterSizeWeightedNormComp;
import moeba.fitnessfunction.impl.BiclusterVarianceNorm;
import moeba.fitnessfunction.impl.MeanSquaredResidue;
import moeba.fitnessfunction.impl.RowVariance;
import moeba.representationwrapper.RepresentationWrapper;
import moeba.representationwrapper.impl.GenericRepresentationWrapper;
import moeba.representationwrapper.impl.IndividualRepresentationWrapper;
import moeba.representationwrapper.impl.SpecificRepresentationWrapper;
import moeba.utils.observer.ProblemObserver.ObserverInterface;
import moeba.utils.observer.impl.BiclusterCountObserver;
import moeba.utils.observer.impl.ExternalCacheObserver;
import moeba.utils.observer.impl.FitnessEvolutionAvgObserver;
import moeba.utils.observer.impl.FitnessEvolutionMaxObserver;
import moeba.utils.observer.impl.FitnessEvolutionMinObserver;
import moeba.utils.observer.impl.InternalCacheObserver;
import moeba.utils.observer.impl.NumEvaluationsObserver;
import moeba.utils.storage.CacheStorage;
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
     * @param cache the internal cache of the fitness function
     * @param summariseIndividualObjectives way to summarise the overall quality of the solutions from the individual quality of their biclusters
     * @return a FitnessFunction object
     */
    public static FitnessFunction getFitnessFunctionFromString(String str, double[][] data, Class<?>[] types, CacheStorage<String, Double> cache, String summariseIndividualObjectives) {
        FitnessFunction res;
        switch (str.toLowerCase()) {

            case "biclustersizenormcomp":
                res = new BiclusterSizeNormComp(data, types, cache, summariseIndividualObjectives);
                break;
            case "biclustersizeweightednormcomp":
                res = new BiclusterSizeWeightedNormComp(data, types, cache, summariseIndividualObjectives);
                break;
            case "biclustervariancenorm":
                res = new BiclusterVarianceNorm(data, types, cache, summariseIndividualObjectives);
                break;
            case "rowvariance":
                res = new RowVariance(data, types, cache, summariseIndividualObjectives);
                break;
            case "meansquaredresidue":
                res = new MeanSquaredResidue(data, types, cache, summariseIndividualObjectives);
                break;
            /**
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
     * Returns an observer from its string representation.
     *
     * @param str string representation of the desired observer
     * @param populationSize the size of the population in the genetic algorithm
     * @param fitnessFunctions an array of strings representing the fitness functions
     * @param numGenerations the number of generations of the genetic algorithm
     * @param externalCache a map of external cache data
     * @param internalCaches an array of internal caches
     * @return an observer
     * @throws RuntimeException if the observer is not implemented
     */
    public static ObserverInterface getObserverFromString(String str, int populationSize, String[] fitnessFunctions, int numGenerations, CacheStorage<String, Double[]> externalCache, CacheStorage<String, Double>[] internalCaches) {
        ObserverInterface res;
        switch (str.toLowerCase()) {
            case "biclustercountobserver":
                res = new BiclusterCountObserver(populationSize, numGenerations);
                break;
            case "externalcacheobserver":
                res = new ExternalCacheObserver(populationSize, externalCache);
                break;
            case "fitnessevolutionminobserver":
                res = new FitnessEvolutionMinObserver(populationSize, fitnessFunctions.length);
                break;
            case "fitnessevolutionmaxobserver":
                res = new FitnessEvolutionMaxObserver(populationSize, fitnessFunctions.length);
                break;
            case "fitnessevolutionavgobserver":
                res = new FitnessEvolutionAvgObserver(populationSize, fitnessFunctions.length);
                break;
            case "internalcacheobserver":
                res = new InternalCacheObserver(populationSize, fitnessFunctions, internalCaches);
                break;
            case "numevaluationsobserver":
                res = new NumEvaluationsObserver(populationSize);
                break;
            default:
                throw new RuntimeException("The observer " + str + " is not implemented.");
        }
        return res;
    }


    /**
     * Returns a new representation wrapper instance based on the given representation.
     * 
     * @param rep The representation to create a wrapper for.
     * @param numRows The number of rows in the dataset.
     * @param numCols The number of columns in the dataset.
     * @param specificNumBiclusters The number of biclusters in the specific representation.
     * @param genericInitialMinPercBics The initial minimum percentage of biclusters in the generic representation.
     * @param genericInitialMaxPercBics The initial maximum percentage of biclusters in the generic representation.
     * @param summariseIndividualObjectives The way to summarise the overall quality of the solutions from the individual quality of their biclusters.
     * @return A new representation wrapper instance.
     * @throws RuntimeException If the given representation is not implemented.
     */
    public static RepresentationWrapper getRepresentationWrapperFromRepresentation(Representation rep, int numRows, int numCols, int specificNumBiclusters, float genericInitialMinPercBics, float genericInitialMaxPercBics, String summariseIndividualObjectives) {
        RepresentationWrapper res;
        switch (rep) {
            case GENERIC:
                res = new GenericRepresentationWrapper(numRows, numCols, genericInitialMinPercBics, genericInitialMaxPercBics, summariseIndividualObjectives);
                break;
            case SPECIFIC:
                res = new SpecificRepresentationWrapper(numRows, numCols, specificNumBiclusters, summariseIndividualObjectives);
                break;
            case INDIVIDUAL:
                res = new IndividualRepresentationWrapper(numRows, numCols);
                break;
            default:
                throw new RuntimeException("The representation " + rep.name() + " is not implemented.");
        }
        return res;
    }

    /**
     * Converts a CSV file to a bidimensional array of strings.
     * 
     * @param inputDataset The input CSV file to read
     * @return A bidimensional array of strings, where each row corresponds to a line in the CSV file and each column
     *         corresponds to a value in that line.
     * @throws IOException If there is an error reading the input CSV file
     */
    public static String[][] csvToStringMatrix(File inputDataset) throws IOException {
        // Read all lines from the CSV file sequentially
        List<String> lines = Files.readAllLines(inputDataset.toPath());
        
        // Process lines in parallel to convert them to an array of objects
        String[][] matrix = lines.parallelStream()
            .skip(1) // Ignore the first line as it contains the heading
            .map(line -> line.split(",")) // Assumes comma as CSV separator
            .toArray(String[][]::new); // Convert list to a bidimensional array of strings
        
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
        typeMapping.put("float64", Float.class);
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
     * Converts a data matrix with string values to a matrix with numeric values.
     * Supported types: string, float, double, int, boolean.
     * Strings are converted to categorical values, with each unique string value
     * being assigned a unique numeric value.
     *
     * @param data The data matrix with string values
     * @param types The types of each column in the data matrix
     * @param numThreads The number of threads to use for parallel processing
     * @return A matrix with numeric values
     */
    public static double[][] dataToNumericMatrix(String[][] data, Class<?>[] types, int numThreads) {
        double[][] numericData = new double[data.length][data[0].length];
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Convert each column in parallel
        for (int j = 0; j < data[0].length; j++) {
            final int colIndex = j;
            executor.execute(() -> {
                // Determine how to convert the data in this column based on its type
                if (types[colIndex] == Float.class || types[colIndex] == Double.class
                        || types[colIndex] == Integer.class) {
                    // Column is numeric, directly parse the values as doubles
                    for (int i = 0; i < data.length; i++) {
                        numericData[i][colIndex] = Double.parseDouble(data[i][colIndex]);
                    }
                } else if (types[colIndex] == Boolean.class) {
                    // Column is boolean, map "Yes" to 1.0 and other values to 0.0
                    for (int i = 0; i < data.length; i++) {
                        numericData[i][colIndex] = data[i][colIndex].equalsIgnoreCase("Yes") ? 1.0 : 0.0;
                    }
                } else if (types[colIndex] == String.class) {
                    // Column is string, convert to categorical values
                    Map<String, Double> categoryToNumber = new HashMap<>();
                    double categoryIndex = 0.0;
                    for (int i = 0; i < data.length; i++) {
                        String category = data[i][colIndex];
                        if (!categoryToNumber.containsKey(category)) {
                            categoryToNumber.put(category, categoryIndex);
                            categoryIndex++;
                        }
                        numericData[i][colIndex] = categoryToNumber.get(category);
                    }
                }
            });
        }
        executor.shutdown();
        return numericData;
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

            } else if (strAlgorithm.equals("NSGAII-ExternalFile-AsyncParallel")) {
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
     * Converts a bicluster to a string representation
     * 
     * @param bicArray the bicluster
     * @return the string representation of the bicluster
     */
    public static String biclusterToString(ArrayList<Integer>[] bicArray) {
        StringBuilder builder = new StringBuilder();
        builder.append("(rows: [");
        for (int i = 0; i < bicArray[0].size(); i++) {
            builder.append(bicArray[0].get(i).toString());
            if (i < bicArray[0].size() - 1) {
                builder.append(" ");
            }
        }
        builder.append("] cols: [");
        for (int i = 0; i < bicArray[1].size(); i++) {
            builder.append(bicArray[1].get(i).toString());
            if (i < bicArray[1].size() - 1) {
                builder.append(" ");
            }
        }
        builder.append("])");
        return builder.toString();
    }

    /**
     * Converts a list of biclusters to a string representation. The string representation
     * is a comma separated list of the string representation of each bicluster.
     * 
     * @param biclusters The list of biclusters to convert to a string
     * @return The string representation of the list of biclusters
     */
    public static String biclustersToString(ArrayList<ArrayList<Integer>[]> biclusters) {
        StringBuilder builder = new StringBuilder();
        for (ArrayList<Integer>[] bicluster : biclusters) {
            builder.append(biclusterToString(bicluster));
            builder.append(", ");
        }
        String res = builder.toString();
        return res.substring(0, res.length() - 2); // Remove last comma and space
    }

    /**
     * Converts a string representation of biclusters into a list of biclusters.
     * Each bicluster is represented as an ArrayList of two ArrayLists of Integers,
     * where the first list represents rows and the second list represents columns.
     *
     * @param biclustersString The string representation of biclusters.
     * @return The ArrayList of biclusters.
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<ArrayList<Integer>[]> stringToBiclusters(String biclustersString) {
        ArrayList<ArrayList<Integer>[]> biclusters = new ArrayList<>();
        Pattern bicPattern = Pattern.compile("Bicluster\\d+: \\(rows: \\[([\\d ]+)\\] cols: \\[([\\d ]+)\\]\\)");
        Matcher matcher = bicPattern.matcher(biclustersString);

        while (matcher.find()) {
            ArrayList<Integer>[] bicArray = new ArrayList[2];
            bicArray[0] = stringToIntegerList(matcher.group(1));
            bicArray[1] = stringToIntegerList(matcher.group(2));
            biclusters.add(bicArray);
        }

        return biclusters;
    }

    /**
     * Converts a space-separated string of numbers into an ArrayList of Integers.
     *
     * @param numbers String containing space-separated numbers.
     * @return ArrayList of Integers parsed from the string.
     */
    private static ArrayList<Integer> stringToIntegerList(String numbers) {
        ArrayList<Integer> list = new ArrayList<>();
        String[] tokens = numbers.trim().split(" ");
        for (String token : tokens) {
            if (!token.isEmpty()) {
                list.add(Integer.parseInt(token));
            }
        }
        return list;
    }


    /**
     * Loads biclusters from a specified CSV file.
     * This method reads a file line by line, converts each line into a list of biclusters,
     * and returns a list of these biclusters. Each bicluster is represented as an ArrayList of ArrayLists of arrays of Integers.
     *
     * @param file The file to read biclusters from.
     * @return A list containing the biclusters loaded from the file.
     * @throws IOException if an I/O error occurs reading from the file.
     */
    public static ArrayList<ArrayList<ArrayList<Integer>[]>> loadBiclusters(File file) {
        ArrayList<ArrayList<ArrayList<Integer>[]>> biclusters = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                biclusters.add(StaticUtils.stringToBiclusters(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return biclusters;
    }

    /**
     * Loads gold standard biclusters from a specified CSV file.
     * This method reads the first line of the file and converts it into a list of biclusters,
     * which is returned. This list is intended to serve as the "gold standard" for validation.
     *
     * @param file The file to read the gold standard biclusters from.
     * @return A list of gold standard biclusters.
     * @throws IOException if an I/O error occurs reading from the file.
     */
    public static ArrayList<ArrayList<Integer>[]> loadGoldStandardBiclusters(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            return StaticUtils.stringToBiclusters(line);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
