package moeba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.impl.NaryTournamentSelection;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "RunnerMOEBA", description = "Multi-Objective Evolutionary Biclustering Algorithm (MOEBA) for Heterogeneous Clinical Data (HeCliDa) with progressive representation for self-determination on the number of clusters", mixinStandardHelpOptions = true)
public class Runner extends AbstractAlgorithmRunner implements Runnable {

    @Option(names = {"--input-dataset"}, description = "Path to the input CSV dataset on which you want to perform biclustering", required = true)
    private File inputDataset;

    @Option(names = {"--input-column-types"}, description = "Path to the input JSON file which specifies the names of the columns in order and the type of data of each of them")
    private File inputColumnTypes;

    @Option(names = {"--str-fitness-functions"}, description = "Objectives to optimize separated by semicolon. Possible values: BiclusterSize, BiclusterVariance, BiclusterRowVariance, MeanSquaredResidue, ScalingMeanSquaredResidue, AverageCorrelationFunction, AverageCorrelationValue, VirtualError, CoefficientOfVariationFunction", defaultValue = "BiclusterSize;BiclusterRowVariance;MeanSquaredResidue")
    private String strFitnessFormulas;

    @Option(names = {"--population-size"}, description = "Population size", defaultValue = "100")
    private int populationSize;

    @Option(names = {"--max-evaluations"}, description = "Max number of evaluations", defaultValue = "25000")
    private int maxEvaluations;

    @Option(names = {"--str-algorithm"}, description = "Algorithm as a string. Possible values: GA-AsyncParallel (mono-objective), NSGAII-AsyncParallel (multi-objective), SMPSO-SyncParallel (multi-objective), NSGAIIExternalFile-AsyncParallel (many-objective)", defaultValue = "NSGAII-AsyncParallel")
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

        // Declare the main execution variables.
        Problem problem;
        CrossoverOperator<IntegerSolution> crossover;
        MutationOperator<IntegerSolution> mutation;
        NaryTournamentSelection<IntegerSolution> selection;
        
        // Read the input dataset
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

        // Read the column types
        Class<?>[] types = null;
        try {
            types = StaticUtils.jsonToClassArray(inputColumnTypes, columnNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
