package moeba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.NaryTournamentSelection;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import moeba.StaticUtils.AlgorithmResult;
import moeba.utils.observer.ProblemObserver;
import moeba.utils.observer.ProblemObserver.ObserverInterface;
import moeba.utils.output.SolutionListTranslatedVAR;
import moeba.utils.output.SolutionListVARWithHeader;
import moeba.utils.storage.CacheStorage;
import moeba.utils.storage.impl.HybridCache;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "RunnerMOEBA", description = "Multi-Objective Evolutionary Biclustering Algorithm (MOEBA) for Heterogeneous Clinical Data (HeCliDa) with progressive representation for self-determination on the number of clusters", mixinStandardHelpOptions = true)
public class Runner extends AbstractAlgorithmRunner implements Runnable {

    @Option(names = {"--input-dataset"}, description = "Path to the input CSV dataset on which you want to perform biclustering", required = true)
    private File inputDataset;

    @Option(names = {"--input-column-types"}, description = "Path to the input JSON file which specifies the names of the columns in order and the type of data of each of them", required = true)
    private File inputColumnTypes;

    @Option(names = {"--representation"}, description = "Representation as a string. Possible values: GENERIC, SPECIFIC, INDIVIDUAL, DYNAMIC", defaultValue = "GENERIC")
    private Representation representation;

    @Option(names = {"--num-biclusters"}, description = "Number of biclusters. Only for SPECIFIC representation", defaultValue = "-1")
    private int numBiclusters;

    @Option(names = {"--str-fitness-functions"}, description = "Objectives to optimize separated by semicolon. Possible values: BiclusterSize, BiclusterVariance, BiclusterRowVariance, MeanSquaredResidue, ScalingMeanSquaredResidue, AverageCorrelationFunction, AverageCorrelationValue, VirtualError, CoefficientOfVariationFunction", defaultValue = "BiclusterSize;BiclusterRowVariance;MeanSquaredResidue")
    private String strFitnessFormulas;

    @Option(names = {"--population-size"}, description = "Population size", defaultValue = "100")
    private int populationSize;

    @Option(names = {"--max-evaluations"}, description = "Max number of evaluations", defaultValue = "25000")
    private int maxEvaluations;

    @Option(names = {"--str-algorithm"}, 
            description = "Algorithm as a string. Possible values: \n" + //
                "\t- Single Objective: GA-AsyncParallel, GA-SingleThread \n" + //
                "\t- Multi Objective: NSGAII-AsyncParallel, NSGAII-SingleThread \n" + //
                "\t- Many Objective: NSGAII-ExternalFile-AsyncParallel", 
            defaultValue = "NSGAII-AsyncParallel")
    private String strAlgorithm;

    @Option(names = {"--crossover-probability"}, description = "Crossover probability", defaultValue = "0.9")
    private double crossoverProbability;

    @Option(names = {"--mutation-probability"}, description = "Mutation probability", defaultValue = "0.1")
    private double mutationProbability;

    @Option(names = {"--crossover-operator"}, 
            description = "Crossover operator. The following are configuration templates according to each type of representation:\n" + //
                "\t- GENERIC: RowPermutationCrossover;BiclusterBinaryCrossover;CellBinaryCrossover or RowBiclusterMixedCrossover;CellBinaryCrossover\n" + //
                "\t- SPECIFIC: ...\n" + //
                "\t- INDIVIDUAL: ...\n" + //
                "\t- DYNAMIC: GENERIC-SPECIFIC\n" + //
                "In case any operator requires additional parameters, they shall be specified in brackets in the following way OperatorName(parameter1=value, parameter2=value, ...)",
            defaultValue = "GroupedBasedCrossover;CellUniformCrossover")
    private String strCrossoverOperator;

    @Option(names = {"--mutation-operator"}, 
            description = "Mutation operator. Same explanation as for the crossover operator:\n" + //
                "\t- GENERIC: RowPermutationMutation;BiclusterBinaryMutation;CellBinaryMutation\n" + //
                "\t- SPECIFIC: ...\n" + //
                "\t- INDIVIDUAL: ...\n" + //
                "\t- DYNAMIC: GENERIC-SPECIFIC", 
            defaultValue = "SwapMutation;BicUniformMutation;CellUniformMutation")
    private String strMutationOperator;

    @Option(names = {"--have-external-cache"}, description = "Whether the external cache is used")
    private boolean haveExternalCache;

    @Option(names = {"--have-internal-cache"}, description = "Whether the internal cache is used")
    private boolean haveInternalCache;

    @Option(names = {"--observers"}, description = "List of observers separated by semicolon. Possible values: BiclusterCountObserver, FitnessEvolutionObserver, NumEvaluationsObserver, ExternalCacheObserver, InternalCacheObserver", defaultValue = "BiclusterCountObserver;FitnessEvolutionObserver;NumEvaluationsObserver;ExternalCacheObserver;InternalCacheObserver")
    private String strObservers;

    @Option(names = {"--num-threads"}, description = "Number of threads. Default value = all")
    private int numThreads = Runtime.getRuntime().availableProcessors();

    @Option(names = {"--output-folder"}, description = "Output folder", defaultValue = "./MOEBA-HeCliDa/")
    private String outputFolder;

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        // Config sort. NOTE: https://github.com/jMetal/jMetal/issues/446
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        // If the representation is not SPECIFIC the number of biclusters cant be set
        if (this.representation != Representation.SPECIFIC && this.numBiclusters != -1) {
            throw new IllegalArgumentException("No se puede fijar el número de biclusters para la representación " + this.representation);
        }

        // Read input dataset
        Object[][] data = null;
        try {
            data = StaticUtils.csvToObjectMatrix(inputDataset);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read column names from input CSV file
        String[] columnNames = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputDataset));
            columnNames = br.readLine().split(",");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read column types
        Class<?>[] types = null;
        try {
            types = StaticUtils.jsonToClassArray(inputColumnTypes, columnNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create Hybrid Caches Manager
        CacheManager hybridCacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        hybridCacheManager.init();

        // Evolución central con representación genérica
        // 1. Array de funciones de fitness
        String[] fitnessFunctions = strFitnessFormulas.split(";");

        // 2. Caché externa
        CacheStorage<String, Double[]> externalCache = haveExternalCache ? new HybridCache<>(hybridCacheManager, "ExternalCache", String.class, Double[].class, 1000) : null;

        // 3. Cachés internas
        CacheStorage<String, Double>[] internalCaches = null;
        if (haveInternalCache) {
            internalCaches = new CacheStorage[fitnessFunctions.length];
            for (int i = 0; i < internalCaches.length; i++) {
                internalCaches[i] = new HybridCache<>(hybridCacheManager, fitnessFunctions[i] + "Cache", String.class, Double.class, 1000);
            }
        }

        // 4. Observadores
        String[] strObserversArray = strObservers.split(";");
        ObserverInterface[] observers = new ObserverInterface[strObserversArray.length];
        for (int i = 0; i < observers.length; i++) {
            observers[i] = StaticUtils.getObserverFromString(strObserversArray[i], populationSize, fitnessFunctions, maxEvaluations / populationSize, externalCache, internalCaches);
        }

        // Problem
        Problem problem = new ProblemObserver(observers, data, types, fitnessFunctions, externalCache, internalCaches);

        // Operators
        // 1. Crossover
        CrossoverOperator<CompositeSolution> crossover = StaticUtils.getCrossoverFromString(crossoverProbability, strCrossoverOperator, representation, (int) Math.round(maxEvaluations * crossoverProbability));
        
        // 2. Mutation
        MutationOperator<CompositeSolution> mutation = StaticUtils.getMutationFromString(mutationProbability, strMutationOperator, representation);

        // 3. Selection
        NaryTournamentSelection<CompositeSolution> selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

        // Algorithm
        AlgorithmResult result = StaticUtils.executeEvolutionaryAlgorithm(
                problem,
                populationSize,
                maxEvaluations,
                strAlgorithm,
                selection,
                crossover,
                mutation,
                numThreads
        );

        // Create output folder
        try {
            Files.createDirectories(Paths.get(outputFolder));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        // Write the evolution of fitness values to an output txt file
        for (ObserverInterface observer : observers) {
            observer.writeToFile(outputFolder + observer.getClass().getSimpleName() +".csv");
        }

        // Write the data of the last population (pareto front approximation)
        String[] varLabels = new String[2 * data.length + data.length * data[0].length];
        for (int i = 0; i < data.length; i++) {
            varLabels[i] = "R" + i;
        }
        for (int i = 0; i < data.length; i++) {
            varLabels[i + data.length] = "P" + i;
        }
        for (int i = 2*data.length; i < varLabels.length; i++) {
            varLabels[i] = "Cell-R" + (i % data.length) + "-C" + ((i / data.length) - 2);
        }
        new SolutionListVARWithHeader(result.population, strFitnessFormulas.split(";"), varLabels)
                .setVarFileOutputContext(new DefaultFileOutputContext(outputFolder + "/VAR.csv", ","))
                .setFunFileOutputContext(new DefaultFileOutputContext(outputFolder + "/FUN.csv", ","))
                .print();

        // Write translated VAR
        new SolutionListTranslatedVAR(Representation.GENERIC, data.length, data[0].length)
            .printTranslatedVAR(outputFolder + "/VAR-translated.csv", result.population);


        System.out.println("Threads used: " + numThreads);
        System.out.println("Total execution time: " + result.computingTime + "ms");
            
        System.exit(0);
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Runner());
        commandLine.execute(args);
    }
}
