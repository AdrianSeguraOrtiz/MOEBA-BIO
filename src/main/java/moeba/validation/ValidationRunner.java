package moeba.validation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import moeba.Representation;
import moeba.StaticUtils;
import moeba.validation.metric.MetricInterface;
import moeba.validation.metric.impl.ClusteringErrorComplementary;
import moeba.validation.metric.impl.ScoreAyadi;
import moeba.validation.metric.impl.ScoreDice;
import moeba.validation.metric.impl.ScoreLiuWang;
import moeba.validation.metric.impl.scoreeren.impl.ScoreErenRecovery;
import moeba.validation.metric.impl.scoreeren.impl.ScoreErenRelevance;
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

    @Option(names = {"--representation"}, description = "Representation as a string. Possible values: GENERIC, SPECIFIC, INDIVIDUAL", defaultValue = "GENERIC")
    private Representation representation;

    @Option(names = {"--gold-standard-translated"}, description = "Path to the input CSV file with gold standard translated biclusters.", required = true)
    private File goldStandardTranslatedFile;

    @Option(names = {"--validation-metrics"}, description = "List of validation metrics, separated by semicolon. Supported values: ScorePrelicRelevance, ScorePrelicRecovery, ScoreLiuWang, ScoreDice, ScoreAyadi, ScoreErenRelevance, ScoreErenRecovery, ClusteringErrorComplementary.", required = true)
    private String validationMetrics;

    @Option(names = {"--save-process"}, description = "Save process to file")
    private boolean saveProcess;

    @Option(names = {"--num-threads"}, description = "Number of threads to use. Defaults to available processors.")
    private int numThreads = Runtime.getRuntime().availableProcessors();

    @Option(names = {"--output-file"}, description = "Path to the output CSV file to save results.", defaultValue = "scores.csv")
    private File outputFile;

    /**
     * The main method that executes the validation logic.
     * It reads the biclusters from files, computes validation scores using specified metrics,
     * and writes the results to an output file.
     */
    @Override
    public void run() {
        // Load inferred biclusters from CSV file
        ArrayList<ArrayList<ArrayList<Integer>[]>> inferredBiclusters = StaticUtils.loadBiclusters(inferredTranslatedFile);

        // Flatten inferred biclusters if representation is INDIVIDUAL
        if (representation == Representation.INDIVIDUAL) {
            ArrayList<ArrayList<Integer>[]> flattenedList = new ArrayList<>();

            for (ArrayList<ArrayList<Integer>[]> list : inferredBiclusters) {
                for (ArrayList<Integer>[] array : list) {
                    flattenedList.add(array);
                }
            }

            inferredBiclusters = new ArrayList<>();
            inferredBiclusters.add(flattenedList);
        }

        // Create output folder if it doesn't exist
        String parentFolderName = outputFile.getParent();
        if (parentFolderName != null) {
            File outputFolder = new File(parentFolderName);
            if (!outputFolder.exists()) outputFolder.mkdirs();
        } else {
            parentFolderName = "./";
        }

        // Load gold standard biclusters from CSV file
        ArrayList<ArrayList<Integer>[]> goldStandardBiclusters = StaticUtils.loadGoldStandardBiclusters(goldStandardTranslatedFile);

        // Parse validation metrics from command-line argument
        MetricInterface[] metrics = parseValidationMetrics(validationMetrics, saveProcess, parentFolderName + "/process/");

        // Compute validation scores for each inferred bicluster
        double[][] scores = computeScores(inferredBiclusters, goldStandardBiclusters, metrics, numThreads);

        // Write the computed scores to an output CSV file
        writeScoresToFile(scores, validationMetrics.split(";"), outputFile);

        // If saveProcess is true, save the process to a zip file
        if (saveProcess) {
            try {
                File dir = new File(parentFolderName + "/process/");
                ZipUtil.pack(dir, new File(parentFolderName + "/process.zip"));
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Parses validation metrics from a string of metric names.
     * Each metric specified in the input string should correspond to a class implementing the MetricInterface.
     * This method maps string names to corresponding MetricInterface implementations.
     *
     * @param metrics A semicolon-separated string containing the names of the metrics.
     * @param saveProcess Whether to save the process to a zip file.
     * @param parentFolderName The parent folder name.
     * @return An array of MetricInterface implementations corresponding to the names provided.
     * @throws IllegalArgumentException if a specified metric name does not correspond to a known implementation.
     */
    private static MetricInterface[] parseValidationMetrics(String metrics, boolean saveProcess, String parentFolderName) {
        String[] metricNames = metrics.split(";");
        MetricInterface[] metricInterfaces = new MetricInterface[metricNames.length];
        for (int i = 0; i < metricNames.length; i++) {
            switch (metricNames[i].toLowerCase()) {
                case "scoreprelicrelevance":
                    metricInterfaces[i] = new ScorePrelicRelevance(saveProcess, parentFolderName);
                    break;
                case "scoreprelicrecovery":
                    metricInterfaces[i] = new ScorePrelicRecovery(saveProcess, parentFolderName);
                    break;
                case "scoreliuwang":
                    metricInterfaces[i] = new ScoreLiuWang(saveProcess, parentFolderName);
                    break;
                case "scoredice":
                    metricInterfaces[i] = new ScoreDice(saveProcess, parentFolderName);
                    break;
                case "scoreayadi":
                    metricInterfaces[i] = new ScoreAyadi(saveProcess, parentFolderName);
                    break;
                case "scoreerenrelevance":
                    metricInterfaces[i] = new ScoreErenRelevance(saveProcess, parentFolderName);
                    break;
                case "scoreerenrecovery":
                    metricInterfaces[i] = new ScoreErenRecovery(saveProcess, parentFolderName);
                    break;
                case "clusteringerrorcomplementary":
                    metricInterfaces[i] = new ClusteringErrorComplementary(saveProcess, parentFolderName);
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
    public static double[][] computeScores(ArrayList<ArrayList<ArrayList<Integer>[]>> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandard, MetricInterface[] metrics, int numThreads) {
        double[][] scores = new double[inferredBiclusters.size()][metrics.length];
        ForkJoinPool customThreadPool = new ForkJoinPool(numThreads);
        try {
            customThreadPool.submit(() -> IntStream.range(0, inferredBiclusters.size()).parallel().forEach(i -> {
                for (int j = 0; j < metrics.length; j++) {
                    scores[i][j] = metrics[j].run(inferredBiclusters.get(i), goldStandard, i);
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
    private static void writeScoresToFile(double[][] scores, String[] metricNames, File outputFile) {
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
