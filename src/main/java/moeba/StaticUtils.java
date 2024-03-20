package moeba;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import moeba.algorithm.AsyncMultiThreadGAParents;
import moeba.algorithm.AsyncMultiThreadNSGAIIParents;
import moeba.algorithm.AsyncMultiThreadNSGAIIParentsExternalFile;
import moeba.fitnessfunction.FitnessFunction;
import moeba.fitnessfunction.impl.BiclusterSize;
import moeba.fitnessfunction.impl.BiclusterSizeWeighted;
import moeba.operator.crossover.generic.GenericCrossover;
import moeba.operator.crossover.generic.biclusterbinary.BiclusterBinaryCrossover;
import moeba.operator.crossover.generic.biclusterbinary.impl.BicUniformCrossover;
import moeba.operator.crossover.generic.cellbinary.CellBinaryCrossover;
import moeba.operator.crossover.generic.cellbinary.impl.CellUniformCrossover;
import moeba.operator.crossover.generic.rowbiclustermixed.RowBiclusterMixedCrossover;
import moeba.operator.crossover.generic.rowbiclustermixed.impl.GroupedBasedCrossover;
import moeba.operator.crossover.generic.rowpermutation.RowPermutationCrossover;
import moeba.operator.crossover.generic.rowpermutation.impl.CycleCrossover;
import moeba.operator.crossover.generic.rowpermutation.impl.EdgeRecombinationCrossover;
import moeba.operator.crossover.generic.rowpermutation.impl.PartiallyMappedCrossover;
import moeba.operator.mutation.generic.GenericMutation;
import moeba.operator.mutation.generic.biclusterbinary.BiclusterBinaryMutation;
import moeba.operator.mutation.generic.biclusterbinary.impl.BicUniformMutation;
import moeba.operator.mutation.generic.cellbinary.CellBinaryMutation;
import moeba.operator.mutation.generic.cellbinary.impl.CellUniformMutation;
import moeba.operator.mutation.generic.rowpermutation.RowPermutationMutation;
import moeba.operator.mutation.generic.rowpermutation.impl.SwapMutation;
import moeba.utils.observer.ProblemObserver.ObserverInterface;
import moeba.utils.observer.impl.BiclusterCountObserver;
import moeba.utils.observer.impl.ExternalCacheObserver;
import moeba.utils.observer.impl.FitnessEvolutionObserver;
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
    public static FitnessFunction getFitnessFunctionFromString(String str, Object[][] data, Class<?>[] types, CacheStorage<String, Double> cache) {
        FitnessFunction res;
        switch (str.toLowerCase()) {

            case "biclustersize":
                res = new BiclusterSize(data, types, cache);
                break;
            case "biclustersizeweighted":
                res = new BiclusterSizeWeighted(data, types, cache);
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
            case "fitnessevolutionobserver":
                res = new FitnessEvolutionObserver(populationSize, fitnessFunctions.length);
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
     * Creates a crossover operator from its string representation.
     * The string representation of the crossover operator is a semicolon-separated list
     * of crossover operators.
     * @param crossoverProbability probability of applying the crossover operator
     * @param strCrossoverOperator string representation of the desired crossover operator
     * @param representation representation of the problem
     * @param numApproxCrossovers number of approximate crossovers to perform
     * @return a crossover operator
     * @throws RuntimeException if the crossover operator is not implemented or the number of crossover operators is not supported for the representation
     */
    public static CrossoverOperator<CompositeSolution> getCrossoverFromString(double crossoverProbability, String strCrossoverOperator, Representation representation, int numApproxCrossovers) {
        CrossoverOperator<CompositeSolution> res;
        String[] listStrCrossovers = strCrossoverOperator.split(";");
        if (representation == Representation.GENERIC) {
            if (listStrCrossovers.length == 2) {
                RowBiclusterMixedCrossover rowBiclusterMixedCrossover = getRowBiclusterMixedCrossoverFromString(listStrCrossovers[0], numApproxCrossovers);
                CellBinaryCrossover cellBinaryCrossover = getCellBinaryCrossoverFromString(listStrCrossovers[1]);
                res = new GenericCrossover(crossoverProbability, rowBiclusterMixedCrossover, cellBinaryCrossover);
            } else if (listStrCrossovers.length == 3) {
                RowPermutationCrossover rowPermutationCrossover = getRowPermutationCrossoverFromString(listStrCrossovers[0]);
                BiclusterBinaryCrossover biclusterBinaryCrossover = getBiclusterBinaryCrossoverFromString(listStrCrossovers[1]);
                CellBinaryCrossover cellBinaryCrossover = getCellBinaryCrossoverFromString(listStrCrossovers[2]);
                res = new GenericCrossover(crossoverProbability, rowPermutationCrossover, biclusterBinaryCrossover, cellBinaryCrossover);
            } else {
                throw new RuntimeException("The number of crossover operators is not supported for the " + representation + " representation.");
            }
        } else if (representation == Representation.SPECIFIC) {
            // TODO: implement
            res = null;
        } else if (representation == Representation.INDIVIDUAL) {
            // TODO: implement
            res = null;
        } else {
            throw new RuntimeException("The representation " + representation + " does not have crossover operators.");
        }

        return res;
    }

    /**
     * Creates a row permutation crossover operator from its string representation.
     * @param str string representation of the desired crossover operator
     * @return a row permutation crossover operator
     * @throws RuntimeException if the crossover operator is not implemented
     */
    public static RowPermutationCrossover getRowPermutationCrossoverFromString(String str) {
        RowPermutationCrossover res;
        switch (str.toLowerCase()) {
            case "cyclecrossover":
                res = new CycleCrossover();
                break;
            case "edgerecombinationcrossover":
                res = new EdgeRecombinationCrossover();
                break;
            case "partiallymappedcrossover":
                res = new PartiallyMappedCrossover();
                break;
            default:
                throw new RuntimeException("The row permutation crossover " + str + " is not implemented.");
        }
        return res;
    }

    /**
     * Creates a bicluster binary crossover operator from its string representation.
     * @param str string representation of the desired crossover operator
     * @return a bicluster binary crossover operator
     * @throws RuntimeException if the crossover operator is not implemented
     */
    public static BiclusterBinaryCrossover getBiclusterBinaryCrossoverFromString(String str) {
        BiclusterBinaryCrossover res;
        switch (str.toLowerCase()) {
            case "bicuniformcrossover":
                res = new BicUniformCrossover();
                break;
            default:
                throw new RuntimeException(
                        "The bicluster binary crossover " + str + " is not implemented.");
        }
        return res;
    }

    /**
     * Creates a cell binary crossover operator from its string representation.
     * @param str string representation of the desired crossover operator
     * @return a cell binary crossover operator
     * @throws RuntimeException if the crossover operator is not implemented
     */
    public static CellBinaryCrossover getCellBinaryCrossoverFromString(String str) {
        CellBinaryCrossover res;
        switch (str.toLowerCase()) {
            case "celluniformcrossover":
                res = new CellUniformCrossover(); // Selects a subset of rows and columns to be included in the offspring cell
                break;
            default:
                throw new RuntimeException(
                        "The cell binary crossover " + str + " is not implemented.");
        }
        return res;
    }

    /**
     * Creates a row bicluster mixed crossover operator from its string representation.
     *
     * @param str string representation of the desired crossover operator
     * @param numApproxCrossovers number of approximate crossovers to perform
     * @return a row bicluster mixed crossover operator
     * @throws RuntimeException if the crossover operator is not implemented
     */
    public static RowBiclusterMixedCrossover getRowBiclusterMixedCrossoverFromString(String str, int numApproxCrossovers) {
        RowBiclusterMixedCrossover res;
        float shuffleEnd = 0.75f;
        float dynamicStartAmount = 0.25f;
        switch (str.toLowerCase()) {
            case "groupedbasedcrossover":
                res = new GroupedBasedCrossover(numApproxCrossovers, shuffleEnd, dynamicStartAmount);
                break;
            default:
                if (str.toLowerCase().matches("groupedbasedcrossover((.*))")) {
                    String[] strParams = str.split("[()=, ]");
                    for (int i = 0; i < strParams.length; i++) {
                        switch (strParams[i].toLowerCase()) {
                            case "shuffleend":
                                shuffleEnd = Float.parseFloat(strParams[i + 1]);
                                break;
                            case "dynamicstartamount":
                                dynamicStartAmount = Float.parseFloat(strParams[i + 1]);
                                break;
                        }
                    }
                    res = new GroupedBasedCrossover(numApproxCrossovers, shuffleEnd, dynamicStartAmount);
                } else {
                    throw new RuntimeException(
                        "The row bicluster mixed crossover " + str + " is not implemented.");
                }
        }
        return res;
    }

    /**
     * Returns a MutationOperator object based on a given string representation.
     * The string representation of the mutation operator is a semicolon-separated list
     * of mutation operators.
     *
     * @param mutationProbability probability of applying the mutation operator
     * @param strMutationOperator string representation of the desired mutation operator
     * @param representation representation of the problem
     * @param numApproxMutations number of approximate mutations to perform
     * @return a MutationOperator object
     * @throws RuntimeException if the mutation operator is not implemented or the number of mutation operators is not supported for the representation
     */
    public static MutationOperator<CompositeSolution> getMutationFromString(String mutationProbability, String strMutationOperator, Representation representation, int numApproxMutations) {
        MutationOperator<CompositeSolution> res;
        String[] listStrMutations = strMutationOperator.split(";");
        if (representation == Representation.GENERIC) {
            if (listStrMutations.length == 3) {
                RowPermutationMutation rowPermutationMutation = getRowPermutationMutationFromString(listStrMutations[0]);
                BiclusterBinaryMutation biclusterBinaryMutation = getBiclusterBinaryMutationFromString(listStrMutations[1]);
                CellBinaryMutation cellBinaryMutation = getCellBinaryMutationFromString(listStrMutations[2]);
                res = new GenericMutation(mutationProbability, numApproxMutations, rowPermutationMutation, biclusterBinaryMutation, cellBinaryMutation);
            } else {
                throw new RuntimeException("The number of mutation operators is not supported for the " + representation + " representation.");
            }
        } else if (representation == Representation.SPECIFIC) {
            // TODO: implement
            res = null;
        } else if (representation == Representation.INDIVIDUAL) {
            // TODO: implement
            res = null;
        } else {
            throw new RuntimeException("The representation " + representation + " does not have mutation operators.");
        }

        return res;
    }

    /**
     * Creates a bicluster binary mutation from the given string.
     *
     * @param str The string representation of the mutation.
     * @return The created bicluster binary mutation.
     * @throws RuntimeException If the mutation is not implemented.
     */
    public static BiclusterBinaryMutation getBiclusterBinaryMutationFromString(String str) {
        BiclusterBinaryMutation res;
        switch (str.toLowerCase()) {
            case "bicuniformmutation":
                res = new BicUniformMutation();
                break;
            default:
                throw new RuntimeException(
                        "The bicluster binary mutation " + str + " is not implemented.");
        }
        return res;
    }

    /**
     * Creates a cell binary mutation from the given string.
     *
     * @param str The string representation of the mutation.
     * @return The created cell binary mutation.
     * @throws RuntimeException If the mutation is not implemented.
     */
    public static CellBinaryMutation getCellBinaryMutationFromString(String str) {
        CellBinaryMutation res;
        switch (str.toLowerCase()) {
            case "celluniformmutation":
                res = new CellUniformMutation();
                break;
            default:
                throw new RuntimeException(
                        "The cell binary mutation " + str + " is not implemented.");
        }
        return res;
    }

    /**
     * Creates a row permutation mutation from the given string.
     *
     * @param str The string representation of the mutation. Valid options are "swapMutation".
     * @return The created row permutation mutation.
     * @throws RuntimeException If the mutation is not implemented.
     */
    public static RowPermutationMutation getRowPermutationMutationFromString(String str) {
        RowPermutationMutation res;
        switch (str.toLowerCase()) {
            case "swapmutation":
                res = new SwapMutation();
                break;
            default:
                throw new RuntimeException(
                        "The row permutation mutation " + str + " is not implemented.");
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
            
            // Initialize rows, cols, minRows, minRow and precalculatedSums
            ArrayList<Integer> rows = new ArrayList<>();
            ArrayList<Integer> cols = new ArrayList<>();
            ArrayList<Integer> minRows = new ArrayList<>();
            int minRow = numRows;
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
                int row = integerVariables.get(i);
                rows.add(row);
                if (row < minRow) minRow = row;
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

                    // Add minRow to list and reset for next iteration
                    minRows.add(minRow);
                    minRow = numRows;
                }
            }

            // Sort list of biclusters depending on the smallest row
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < minRows.size(); i++) {
                indexes.add(i);
            }
            Collections.sort(indexes, Comparator.comparing(minRows::get));
            ArrayList<ArrayList<Integer>[]> resOrdered = new ArrayList<>();
            for (int index : indexes) {
                resOrdered.add(res.get(index));
            }

            return resOrdered;
        } else {
            // TODO: Implement specific representation
            return null;
        }
    }

    /**
     * Merges biclusters with the same columns, preserving the order of the rows
     * and the order of the columns. The merged biclusters are added to the
     * given solution.
     *
     * @param biclusters the biclusters to merge
     * @param solution the composite solution where the merged biclusters are
     * added
     */
    @SuppressWarnings("unchecked")
    public static void mergeBiclustersSameColumns(ArrayList<ArrayList<Integer>[]> biclusters, CompositeSolution solution) {

        // Mapa para agrupar filas por sus columnas correspondientes
        Map<String, ArrayList<Integer>> map = new LinkedHashMap<>();

        // For each bicluster, add its rows to the map with the columns as key
        for (ArrayList<Integer>[] bicluster : biclusters) {
            String key = bicluster[1].toString(); // column list as string
            map.computeIfAbsent(key, k -> new ArrayList<>())
                .addAll(bicluster[0]); // add rows to column list
        }

        // Clear old biclusters list
        biclusters.clear();

        // Extract integer and binary variables from the composite solution
        List<Integer> integerVariables = ((IntegerSolution) solution.variables().get(0)).variables();
        integerVariables.clear();
        BinarySet binaryVariables = ((BinarySolution) solution.variables().get(1)).variables().get(0);
        binaryVariables.clear();

        // For each group of rows with the same columns, create a new bicluster
        for (Map.Entry<String, ArrayList<Integer>> entry : map.entrySet()) {
            // Get rows and sort them
            ArrayList<Integer> rows = entry.getValue();
            Collections.sort(rows);

            // Reconstruct columns from the key
            String key = entry.getKey();
            ArrayList<Integer> cols = key.length() <= 2 ? new ArrayList<>() : new ArrayList<>(Arrays.asList(key.substring(1, key.length() - 1).split(", ")))
                .stream().map(Integer::parseInt)
                .collect(Collectors.toCollection(ArrayList::new));

            // Create and add the new bicluster
            ArrayList<Integer>[] bicluster = new ArrayList[2];
            bicluster[0] = rows;
            integerVariables.addAll(rows);
            binaryVariables.set(integerVariables.size()-1);
            bicluster[1] = cols;
            biclusters.add(bicluster);
        }
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
}
