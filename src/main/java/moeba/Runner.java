package moeba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.NaryTournamentSelection;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import moeba.StaticUtils.AlgorithmResult;
import moeba.operator.crossover.GenericCrossover;
import moeba.operator.crossover.biclustersbinary.impl.BicUniformCrossover;
import moeba.operator.crossover.cellbinary.impl.CellUniformCrossover;
import moeba.operator.crossover.rowpermutation.impl.CycleCrossover;
import moeba.operator.mutation.GenericMutation;
import moeba.utils.observer.ProblemFitnessEvolution;
import moeba.utils.output.SolutionListTranslatedVAR;
import moeba.utils.output.SolutionListVARWithHeader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "RunnerMOEBA", description = "Multi-Objective Evolutionary Biclustering Algorithm (MOEBA) for Heterogeneous Clinical Data (HeCliDa) with progressive representation for self-determination on the number of clusters", mixinStandardHelpOptions = true)
public class Runner extends AbstractAlgorithmRunner implements Runnable {

    @Option(names = {"--input-dataset"}, description = "Path to the input CSV dataset on which you want to perform biclustering", required = true)
    private File inputDataset;

    @Option(names = {"--input-column-types"}, description = "Path to the input JSON file which specifies the names of the columns in order and the type of data of each of them", required = true)
    private File inputColumnTypes;

    @Option(names = {"--str-fitness-functions"}, description = "Objectives to optimize separated by semicolon. Possible values: BiclusterSize, BiclusterVariance, BiclusterRowVariance, MeanSquaredResidue, ScalingMeanSquaredResidue, AverageCorrelationFunction, AverageCorrelationValue, VirtualError, CoefficientOfVariationFunction", defaultValue = "BiclusterSize;BiclusterRowVariance;MeanSquaredResidue")
    private String strFitnessFormulas;

    @Option(names = {"--population-size"}, description = "Population size", defaultValue = "100")
    private int populationSize;

    @Option(names = {"--max-evaluations"}, description = "Max number of evaluations", defaultValue = "25000")
    private int maxEvaluations;

    @Option(names = {"--str-algorithm"}, description = "Algorithm as a string. Possible values: GA-AsyncParallel (mono-objective), NSGAII-AsyncParallel (multi-objective), NSGAIIExternalFile-AsyncParallel (many-objective)", defaultValue = "NSGAII-AsyncParallel")
    private String strAlgorithm;

    @Option(names = {"--crossover-probability"}, description = "Crossover probability", defaultValue = "0.9")
    private double crossoverProbability;

    @Option(names = {"--mutation-probability"}, description = "Mutation probability", defaultValue = "0.1")
    private double mutationProbability;

    @Option(names = {"--num-threads"}, description = "Number of threads. Default value = all")
    private int numThreads = Runtime.getRuntime().availableProcessors();

    @Option(names = {"--output-folder"}, description = "Output folder", defaultValue = "./MOEBA-HeCliDa/")
    private String outputFolder;

    @Override
    public void run() {
        // Config sort. NOTE: https://github.com/jMetal/jMetal/issues/446
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

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

        // Evolución central con representación genérica
        Problem problem = new ProblemFitnessEvolution(data, types, strFitnessFormulas, populationSize);
        CrossoverOperator<CompositeSolution> crossover = new GenericCrossover(crossoverProbability, new CycleCrossover(), new BicUniformCrossover(), new CellUniformCrossover());
        MutationOperator<CompositeSolution> mutation = new GenericMutation(mutationProbability);
        NaryTournamentSelection<CompositeSolution> selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
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
        ((ProblemFitnessEvolution) problem).writeFitnessEvolution(outputFolder + "/fitness_evolution.txt");

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
