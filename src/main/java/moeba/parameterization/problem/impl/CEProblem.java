package moeba.parameterization.problem.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import moeba.Runner;
import moeba.StaticUtils.AlgorithmResult;
import moeba.parameterization.ParameterizationExercise;
import moeba.parameterization.ParameterizationRunner;
import moeba.parameterization.problem.ParameterizationProblem;
import moeba.validation.ValidationRunner;

public class CEProblem extends ParameterizationProblem {

    private String[] prefixes;
    private ParameterizationExercise subExercise;

    public CEProblem(ParameterizationExercise parameterizationExercise, String staticConf, String[] prefixes, ParameterizationExercise subExercise) {
        super(parameterizationExercise, staticConf);

        this.prefixes = prefixes;
        this.subExercise = subExercise;
    }

    @Override
    public CompositeSolution evaluate(CompositeSolution solution) {

        // Get solution id
        int cnt = super.parallelCount.incrementAndGet();
        String solutionOutputFolder = "./tmp/CE-" + String.format("%03d", cnt) + "/";
        
        // Get arguments
        String solutionArgs = super.getArgsFromSolution(solution);

        // Config HV problem
        String strArgs = staticConf + " " + solutionArgs;
        HVProblem hvproblem = new HVProblem(
            this.subExercise,
            strArgs,
            prefixes,
            solutionOutputFolder
        );

        // Run HV problem
        AlgorithmResult result = ParameterizationRunner.executeParameterizationAlgorithm(hvproblem);

        // Save HV problem results
        new SolutionListOutput(result.population)
            .setVarFileOutputContext(new DefaultFileOutputContext(solutionOutputFolder + "HV-VAR.csv", ","))
            .setFunFileOutputContext(new DefaultFileOutputContext(solutionOutputFolder + "HV-FUN.csv", ","))
            .print();

        // Save translated arguments of the unsupervised best solution for all benchmarks
        CompositeSolution hvsolution = result.population.get(0);
        String varTranslated = hvproblem.getArgsFromSolution(hvsolution);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(solutionOutputFolder + "HV-VAR-translated.csv"))) {
            writer.write(varTranslated);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Run unsupervised best solution for each benchmark to get the clustering errors
        double score = 0.0;
        for (int i = 0; i < prefixes.length; i++) {

            // Config problem
            String benchSolOutputFolder = solutionOutputFolder + "/bench-" + i + "/";
            String strBenchArgs = strArgs + " " + varTranslated + " --input-dataset=" + prefixes[i] + "-data.csv" + " --input-column-types=" + prefixes[i] + "-types.json" + " --output-folder=" + benchSolOutputFolder;
            String[] benchArgs = super.preprocessArguments(strBenchArgs.split(" "));

            // Run MOEBA algorithm
            Runner.main(benchArgs);

            // Get representation
            String representation = "";
            for (int j = 0; j < benchArgs.length; j++) {
                if (benchArgs[j].startsWith("--representation=")) {
                    representation = benchArgs[j].split("=")[1];
                    break;
                }
            }

            // Run validation to get the clustering errors of the solutions in the front of the winner unsupervised configuration
            String winnerVarTranslatedFile = benchSolOutputFolder + "VAR-translated.csv";
            String gsTranslated = prefixes[i] + "-translated.csv";
            String validationArgs = 
                "--inferred-translated=" + winnerVarTranslatedFile +
                " --representation=" + representation +
                " --gold-standard-translated=" + gsTranslated +
                " --validation-metrics=ClusteringErrorComplementary" +
                " --num-threads=1" +
                " --output-file=" + benchSolOutputFolder + "/ClusteringError.csv";
            ValidationRunner.main(validationArgs.split(" "));

            // Read clustering errors
            double[][] clusteringErrors = super.readVectors(benchSolOutputFolder + "/ClusteringError.csv", ",");       
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
 
        // Delete solution output folder
        /**
        try {
            FileUtils.deleteDirectory(new File(solutionOutputFolder));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        if (cnt % parameterizationExercise.populationSize == 0) {
            System.out.println(cnt);
        }

        return solution;
    }
    
}
