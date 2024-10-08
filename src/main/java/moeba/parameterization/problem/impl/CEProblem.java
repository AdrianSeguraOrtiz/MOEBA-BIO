package moeba.parameterization.problem.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import moeba.Representation;
import moeba.StaticUtils;
import moeba.StaticUtils.AlgorithmResult;
import moeba.parameterization.ParameterizationExercise;
import moeba.parameterization.ParameterizationRunner;
import moeba.parameterization.ParameterizationSolution;
import moeba.parameterization.problem.ParameterizationProblem;
import moeba.representationwrapper.RepresentationWrapper;
import moeba.utils.observer.ProblemObserver.ObserverInterface;
import moeba.validation.ValidationRunner;
import moeba.validation.metric.MetricInterface;
import moeba.validation.metric.impl.ClusteringErrorComplementary;

public class CEProblem extends ParameterizationProblem {

    private String[] prefixes;
    private ParameterizationExercise subExercise;
    private String subObservers;
    public int[] numRows;
    public int[] numCols;
    public String strTempFolder;

    public CEProblem(ParameterizationExercise parameterizationExercise, String staticConf, ObserverInterface[] observers, String subObservers, String[] prefixes, ParameterizationExercise subExercise, String strTempFolder) {
        super(parameterizationExercise, staticConf, observers);

        this.subObservers = subObservers;
        this.prefixes = prefixes;
        this.subExercise = subExercise;
        this.numRows = new int[prefixes.length];
        this.numCols = new int[prefixes.length];
        this.strTempFolder = strTempFolder;
        try {
            Files.createDirectories(Paths.get(strTempFolder));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        for (int i = 0; i < prefixes.length; i++) {
            String dataset = prefixes[i] + "-data.csv";
            int rowCount = 0;
            int columnCount = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(dataset))) {
                columnCount = br.readLine().split(",").length - 1;
                while (br.readLine() != null) {
                    rowCount++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.numRows[i] = rowCount;
            this.numCols[i] = columnCount;
        }

    }

    @Override
    public ParameterizationSolution evaluate(ParameterizationSolution solution) { 
        
        // Get arguments
        String solutionArgs = this.parameterizationExercise.getArgsFromSolution(solution);

        // Get representation
        String representation = this.parameterizationExercise.getValueOfArg("--representation", solution);

        // Get number of objectives
        int numObjectives = 0;
        int cntO = 0;
        String value = "";
        while (value != null) {
            value = this.parameterizationExercise.getValueOfArg("--comb--str-fitness-functions--" + cntO, solution);
            if (value != null && !value.equals("''")) {
                numObjectives += value.split(";").length;
            }
            cntO++;
        }

        // Penalize solution if the number of objectives is less than 2
        if (numObjectives < 2) {
            solution.objectives()[0] = 1.0;
            return solution;
        }

        // Get subObservers
        ObserverInterface[] subObserversArray = new ObserverInterface[0];
        if (!this.subObservers.equals("")) {
            String[] subObserversArrayStr = this.subObservers.split(";");
            subObserversArray = new ObserverInterface[subObserversArrayStr.length];
            for (int i = 0; i < subObserversArrayStr.length; i++) {
                subObserversArray[i] = StaticUtils.getObserverFromString(subObserversArrayStr[i], subExercise.populationSize, new String[]{"HV"}, 0, null, null, subExercise);
            }
        }

        // Config HV problem
        String strArgs = staticConf + " " + solutionArgs;
        HVProblem hvproblem = new HVProblem(
            this.subExercise,
            strArgs,
            subObserversArray,
            prefixes,
            numObjectives
        );

        // Run HV problem
        AlgorithmResult<ParameterizationSolution> result = ParameterizationRunner.executeParameterizationAlgorithm(hvproblem);

        // Save HV winner into CE individual
        solution.subPopulations.add(result.population);
        solution.subObservers.add(subObserversArray);

        // Executor service for parallel execution
        ExecutorService executorService = Executors.newFixedThreadPool(prefixes.length);
        List<Future<Double>> futures = new ArrayList<>();

        // Get clustering error obtained by HV winner for each benchmark
        for (int i = 0; i < prefixes.length; i++) {
            final int finalI = i;
            Callable<Double> task = () -> {

                // Get representation wrapper
                RepresentationWrapper wrapper = StaticUtils.getRepresentationWrapperFromRepresentation(Representation.valueOf(representation), numRows[finalI], numCols[finalI], 0, 0, 0, null);

                // Get inferred biclusters from the MOEBA winner population after apply the configuration of HV winner
                int tagIndex = result.population.get(0).tags.indexOf(Paths.get(prefixes[finalI]).getFileName().toString());
                List<ParameterizationSolution> subSolutions = result.population.get(0).subPopulations.get(tagIndex);
                ArrayList<ArrayList<ArrayList<Integer>[]>> inferredBiclusters = new ArrayList<>();
                for (ParameterizationSolution ss : subSolutions) {
                    inferredBiclusters.add(wrapper.getBiclustersFromRepresentation(ss));
                }

                // Run validation to get the clustering errors
                ArrayList<ArrayList<Integer>[]> goldStandard = StaticUtils.loadGoldStandardBiclusters(new File(prefixes[finalI] + "-translated.csv"));
                MetricInterface[] metricInterfaces = new MetricInterface[1];
                metricInterfaces[0] = new ClusteringErrorComplementary(false, null);
                double[][] clusteringErrors = ValidationRunner.computeScores(inferredBiclusters, goldStandard, metricInterfaces, subExercise.numThreads);
                
                // Sort clustering errors
                double[] values = new double[clusteringErrors.length];
                for (int j = 0; j < clusteringErrors.length; j++) {
                    values[j] = clusteringErrors[j][0];
                }
                Arrays.sort(values);

                // Set as score the mean of the best 5 solutions
                double mean = 0.0;
                int div = Math.min(values.length, 5);
                for (int j = values.length - 1; j >= values.length - div; j--) {
                    mean += values[j];
                }

                // Get the complementary of the complementary to get the clustering error
                return  1 - (mean / div);
            };
            futures.add(executorService.submit(task));
        }

        double score = 0.0;
        for (Future<Double> future : futures) {
            try {
                score += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Shutdown executor service
        executorService.shutdown();

        // Evaluate solution
        solution.objectives()[0] = score / prefixes.length;

        // Observers
        this.registerInfo(solution);
        this.writeInfo(strTempFolder);

        return solution;
    }
    
    @Override
    public String getName() {
        return "Clustering Error Problem (supervised)";
    }
}
