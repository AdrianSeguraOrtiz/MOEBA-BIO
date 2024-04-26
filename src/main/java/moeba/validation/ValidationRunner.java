package moeba.validation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import moeba.StaticUtils;
import moeba.validation.metric.MetricInterface;
import moeba.validation.metric.impl.scoreprelic.impl.ScorePrelicRecovery;
import moeba.validation.metric.impl.scoreprelic.impl.ScorePrelicRelevance;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Main class for validation of inferred biclusters against a gold standard using specified metrics.
 * This class uses the picocli library for command-line interaction, allowing users to specify
 * input and output files, as well as the metrics to be used for validation.
 */
@Command(name = "ValidationRunner", description = "Executes validation of translated biclusters against a gold standard.", mixinStandardHelpOptions = true, showDefaultValues = true, sortOptions = false)
public class ValidationRunner implements Runnable {

    @Option(names = {"--inferred-translated"}, description = "Path to the input CSV file with inferred translated biclusters.", required = true)
    private File inferredTranslatedFile;

    @Option(names = {"--gold-standard-translated"}, description = "Path to the input CSV file with gold standard translated biclusters.", required = true)
    private File goldStandardTranslatedFile;

    @Option(names = {"--validation-metrics"}, description = "List of validation metrics, separated by semicolon. Supported values: ScorePrelicRelevance, ScorePrelicRecovery.", required = true)
    private String validationMetrics;

    @Option(names = {"--num-threads"}, description = "Number of threads to use. Defaults to available processors.")
    private int numThreads = Runtime.getRuntime().availableProcessors();

    @Option(names = {"--output-file"}, description = "Path to the output CSV file to save results.", required = true)
    private File outputFile;

    /**
     * The main method that executes the validation logic.
     * It reads the biclusters from files, computes validation scores using specified metrics,
     * and writes the results to an output file.
     */
    @Override
    public void run() {
        // Load inferred biclusters from CSV file
        ArrayList<ArrayList<ArrayList<Integer>[]>> inferredBiclusters = loadBiclusters(inferredTranslatedFile);

        // Load gold standard biclusters from CSV file
        ArrayList<ArrayList<Integer>[]> goldStandardBiclusters = loadGoldStandardBiclusters(goldStandardTranslatedFile);

        // Parse validation metrics from command-line argument
        MetricInterface[] metrics = parseValidationMetrics(validationMetrics);

        // Compute validation scores for each inferred bicluster
        double[][] scores = computeScores(inferredBiclusters, goldStandardBiclusters, metrics, numThreads);

        // Write the computed scores to an output CSV file
        writeScoresToFile(scores, validationMetrics.split(";"), outputFile);
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
    private ArrayList<ArrayList<ArrayList<Integer>[]>> loadBiclusters(File file) {
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
    private ArrayList<ArrayList<Integer>[]> loadGoldStandardBiclusters(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            return StaticUtils.stringToBiclusters(line);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Parses validation metrics from a string of metric names.
     * Each metric specified in the input string should correspond to a class implementing the MetricInterface.
     * This method maps string names to corresponding MetricInterface implementations.
     *
     * @param metrics A semicolon-separated string containing the names of the metrics.
     * @return An array of MetricInterface implementations corresponding to the names provided.
     * @throws IllegalArgumentException if a specified metric name does not correspond to a known implementation.
     */
    private MetricInterface[] parseValidationMetrics(String metrics) {
        String[] metricNames = metrics.split(";");
        MetricInterface[] metricInterfaces = new MetricInterface[metricNames.length];
        for (int i = 0; i < metricNames.length; i++) {
            switch (metricNames[i].toLowerCase()) {
                case "scoreprelicrelevance":
                    metricInterfaces[i] = new ScorePrelicRelevance();
                    break;
                case "scoreprelicrecovery":
                    metricInterfaces[i] = new ScorePrelicRecovery();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported validation metric: " + metricNames[i]);
            }
        }
        return metricInterfaces;
    }

    /**
     * Computes validation scores for each inferred bicluster against the gold standard.
     * Scores are computed in parallel using a specified number of threads. Each score represents
     * how well an inferred bicluster matches against the gold standard according to a specific metric.
     *
     * @param inferredBiclusters A list of inferred biclusters to validate.
     * @param goldStandard The gold standard biclusters against which validation is performed.
     * @param metrics An array of metrics to use for validation.
     * @param numThreads The number of threads to use for validation.
     * @return A 2D array of scores, where each row corresponds to an inferred bicluster and each column to a metric.
     */
    private double[][] computeScores(ArrayList<ArrayList<ArrayList<Integer>[]>> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandard, MetricInterface[] metrics, int numThreads) {
        double[][] scores = new double[inferredBiclusters.size()][metrics.length];
        ForkJoinPool customThreadPool = new ForkJoinPool(numThreads);
        try {
            customThreadPool.submit(() -> IntStream.range(0, inferredBiclusters.size()).parallel().forEach(i -> {
                for (int j = 0; j < metrics.length; j++) {
                    scores[i][j] = metrics[j].getScore(inferredBiclusters.get(i), goldStandard);
                }
            })).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
        }
        return scores;
    }

    /**
     * Writes computed validation scores to a CSV file.
     * This method first writes the names of the metrics as headers, then writes rows of scores,
     * with each row representing an inferred bicluster and each column representing a score under a specific metric.
     *
     * @param scores The 2D array of scores to write.
     * @param metricNames The names of the metrics, used as headers in the CSV.
     * @param outputFile The file to write the scores to.
     * @throws IOException if an I/O error occurs writing to the file.
     */
    private void writeScoresToFile(double[][] scores, String[] metricNames, File outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            for (int i = 0; i < metricNames.length; i++) {
                writer.print(metricNames[i]);
                if (i < metricNames.length - 1) writer.print(",");
            }
            writer.println();
            for (double[] row : scores) {
                for (int j = 0; j < row.length; j++) {
                    writer.print(row[j]);
                    if (j < row.length - 1) writer.print(",");
                }
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new ValidationRunner());
        commandLine.execute(args);
    }
}
