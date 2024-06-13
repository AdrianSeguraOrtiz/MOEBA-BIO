package moeba.parameterization.problem.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moeba.Representation;
import moeba.StaticUtils;
import moeba.StaticUtils.AlgorithmResult;
import moeba.parameterization.ParameterizationExercise;
import moeba.parameterization.ParameterizationRunner;
import moeba.parameterization.ParameterizationSolution;
import moeba.parameterization.problem.ParameterizationProblem;
import moeba.representationwrapper.RepresentationWrapper;
import moeba.validation.ValidationRunner;
import moeba.validation.metric.MetricInterface;
import moeba.validation.metric.impl.ClusteringErrorComplementary;

public class CEProblem extends ParameterizationProblem {

    private String[] prefixes;
    private ParameterizationExercise subExercise;
    public int[] numRows;
    public int[] numCols;

    public CEProblem(ParameterizationExercise parameterizationExercise, String staticConf, String[] prefixes, ParameterizationExercise subExercise) {
        super(parameterizationExercise, staticConf);

        this.prefixes = prefixes;
        this.subExercise = subExercise;
        this.numRows = new int[prefixes.length];
        this.numCols = new int[prefixes.length];

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

        // Get representation and number of objectives
        String representation = this.parameterizationExercise.getValueOfArg("--representation", solution);
        int numObjectives = this.parameterizationExercise.getValueOfArg("--str-fitness-functions", solution).split(";").length;

        // Config HV problem
        String strArgs = staticConf + " " + solutionArgs;
        HVProblem hvproblem = new HVProblem(
            this.subExercise,
            strArgs,
            prefixes,
            numObjectives
        );

        // Run HV problem
        AlgorithmResult<ParameterizationSolution> result = ParameterizationRunner.executeParameterizationAlgorithm(hvproblem);

        // Save HV winner into CE individual
        solution.subPopulations.add(result.population);

        // Get clustering error obtained by HV winner for each benchmark
        double score = 0.0;
        for (int i = 0; i < prefixes.length; i++) {

            // Get representation wrapper
            RepresentationWrapper wrapper = StaticUtils.getRepresentationWrapperFromRepresentation(Representation.valueOf(representation), numRows[i], numCols[i], 0, 0, 0, null);

            // Get inferred biclusters from the MOEBA winner population after apply the configuration of HV winner
            List<ParameterizationSolution> subSolutions = solution.subPopulations.get(0).get(0).subPopulations.get(i);
            ArrayList<ArrayList<ArrayList<Integer>[]>> inferredBiclusters = new ArrayList<>();
            for (ParameterizationSolution ss : subSolutions) {
                inferredBiclusters.add(wrapper.getBiclustersFromRepresentation(ss));
            }

            // Run validation to get the clustering errors
            ArrayList<ArrayList<Integer>[]> goldStandard = StaticUtils.loadGoldStandardBiclusters(new File(prefixes[i] + "-translated.csv"));
            MetricInterface[] metricInterfaces = new MetricInterface[1];
            metricInterfaces[0] = new ClusteringErrorComplementary(false, null);
            double[][] clusteringErrors = ValidationRunner.computeScores(inferredBiclusters, goldStandard, metricInterfaces, 1);
            
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
            score += mean / div;
        }

        // Evaluate solution
        solution.objectives()[0] += score / prefixes.length;

        // Print progress
        int cnt = super.parallelCount.incrementAndGet();
        if (cnt % parameterizationExercise.populationSize == 0) {
            System.out.println(cnt);
        }

        return solution;
    }
    
}
