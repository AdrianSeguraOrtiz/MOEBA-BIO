package moeba.parameterization;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.CompositeCrossover;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.CompositeMutation;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import moeba.StaticUtils;
import moeba.StaticUtils.AlgorithmResult;
import moeba.parameterization.problem.ParameterizationProblem;
import moeba.parameterization.problem.impl.CEProblem;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "RunnerParameterization", description = "Run the MOEBA parameterization", mixinStandardHelpOptions = true, showDefaultValues = true, sortOptions = false)
public class ParameterizationRunner implements Runnable {

    @Option(names = {"--input-benchmark-folder"}, description = "Path to the input benchmark folder", required = true)
    private File inputBenchmarkFolder;

    @Option(names = {"--internal-evaluations"}, description = "Number of internal evaluations", required = true)
    private int internalEvaluations;

    @Option(names = {"--unsupervised-conf-file"}, description = "Path to the unsupervised configuration file", required = true)
    private File unsupervisedConfFile;

    @Option(names = {"--external-unsupervised-evaluations"}, description = "Number of external unsupervised evaluations", required = true)
    private int externalUnsupervisedEvaluations;

    @Option(names = {"--external-unsupervised-population-size"}, description = "External unsupervised population size", required = true)
    private int externalUnsupervisedPopulationSize;

    @Option(names = {"--supervised-conf-file"}, description = "Path to the supervised configuration file", required = true)
    private File supervisedConfFile;

    @Option(names = {"--external-supervised-evaluations"}, description = "Number of external supervised evaluations", required = true)
    private int externalSupervisedEvaluations;

    @Option(names = {"--external-supervised-population-size"}, description = "External supervised population size", required = true)
    private int externalSupervisedPopulationSize;

    @Option(names = {"--num-threads"}, description = "Number of threads. Default: All")
    private int numThreads = Runtime.getRuntime().availableProcessors();

    @Option(names = {"--output-folder"}, description = "Path to the output folder", defaultValue = "./MOEBA-Parameterization/")
    private String outputFolder;

    @Override
    public void run() {
        // Config sort. NOTE: https://github.com/jMetal/jMetal/issues/446
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        // Read input benchmark folder
        Set<String> validPrefixes = detectValidPrefixes(inputBenchmarkFolder);
        System.out.println("Number of problems detected: " + validPrefixes.size());
        System.out.println("Prefixes: " + String.join(", ", validPrefixes));

        // Get parameterization exercises
        ParameterizationExercise supervisedParameterizationExercise = new ParameterizationExercise(supervisedConfFile, externalSupervisedEvaluations, externalSupervisedPopulationSize, 1);
        ParameterizationExercise unsupervisedParameterizationExercise = new ParameterizationExercise(unsupervisedConfFile, externalUnsupervisedEvaluations, externalUnsupervisedPopulationSize, numThreads);
        
        // Get parameterization problem
        String staticConf = "--max-evaluations=" + internalEvaluations + " --num-threads=1" + " --observers=FitnessEvolutionMinObserver";
        CEProblem ceproblem = new CEProblem(supervisedParameterizationExercise, staticConf, validPrefixes.toArray(new String[validPrefixes.size()]), unsupervisedParameterizationExercise);

        // Run parameterization
        AlgorithmResult result = ParameterizationRunner.executeParameterizationAlgorithm(ceproblem);

        // TODO: Save solution

        System.out.println("Threads used: " + numThreads);
        System.out.println("Total execution time: " + result.computingTime + "ms");
            
        System.exit(0);

    }

    /**
     * Executes the parameterization algorithm.
     *
     * @param problem the parameterization problem
     * @return the result of the parameterization algorithm
     */
    public static AlgorithmResult executeParameterizationAlgorithm(
            ParameterizationProblem problem) {

        // Get the exercise
        ParameterizationExercise exercise = problem.getParameterizationExercise();

        // Create the crossover operator.
        // The crossover operator is a composite crossover that combines
        // SBX crossover and integer SBX crossover.
        CrossoverOperator<CompositeSolution> crossover =
                new CompositeCrossover(Arrays.asList(
                        new SBXCrossover(1.0, 20.0),
                        new IntegerSBXCrossover(1.0, 20.0)
                ));

        // Create the mutation operator.
        // The mutation operator is a composite mutation that combines
        // polynomial mutation and integer polynomial mutation.
        MutationOperator<CompositeSolution> mutation =
                new CompositeMutation(Arrays.asList(
                        new PolynomialMutation(0.1, 20.0),
                        new IntegerPolynomialMutation(0.1, 2.0)
                ));

        // Execute the evolutionary algorithm.
        // The algorithm is a genetic algorithm (GA) running in a single thread.
        AlgorithmResult result = StaticUtils.executeEvolutionaryAlgorithm(
                problem,
                exercise.populationSize,
                exercise.evaluations,
                exercise.numThreads == 1 ? "GA-SingleThread" : "GA-AsyncParallel",
                new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>()),
                crossover,
                mutation,
                exercise.numThreads
        );

        // Return the result of the parameterization algorithm.
        return result;
    }


    /**
     * Detects the valid prefixes in the input benchmark folder.
     *
     * @param benchFolder the input benchmark folder
     * @return a set of valid prefixes
     * @throws IllegalArgumentException if the input benchmark folder is not found
     */
    public static Set<String> detectValidPrefixes(File benchFolder) {

        // Check if the input benchmark folder exists
        if (!benchFolder.isDirectory()) {
            throw new IllegalArgumentException("Input benchmark folder not found: " + benchFolder);
        }

        // Map to store the prefixes and the found file types
        Map<String, Set<String>> prefixMap = new HashMap<>();

        // List all the files in the directory
        File[] files = benchFolder.listFiles();

        // Iterate over each file and group them by prefix
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();

                // Check if the file name ends with "-data.csv"
                if (fileName.endsWith("-data.csv")) {
                    String prefix = fileName.substring(0, fileName.length() - "-data.csv".length());
                    prefixMap.computeIfAbsent(prefix, k -> new HashSet<>()).add("data");
                }

                // Check if the file name ends with "-translated.csv"
                else if (fileName.endsWith("-translated.csv")) {
                    String prefix = fileName.substring(0, fileName.length() - "-translated.csv".length());
                    prefixMap.computeIfAbsent(prefix, k -> new HashSet<>()).add("translated");
                }

                // Check if the file name ends with "-types.json"
                else if (fileName.endsWith("-types.json")) {
                    String prefix = fileName.substring(0, fileName.length() - "-types.json".length());
                    prefixMap.computeIfAbsent(prefix, k -> new HashSet<>()).add("types");
                }
            }
        }

        // Find all the prefixes that have all the three types of files
        Set<String> validPrefixes = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : prefixMap.entrySet()) {
            if (entry.getValue().contains("data") && entry.getValue().contains("translated") && entry.getValue().contains("types")) {
                validPrefixes.add(benchFolder + "/" + entry.getKey());
            }
        }

        return validPrefixes;
    }


    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new ParameterizationRunner());
        commandLine.execute(args);
    }
    
}
