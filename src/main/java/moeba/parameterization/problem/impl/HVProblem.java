package moeba.parameterization.problem.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.uma.jmetal.experimental.qualityIndicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

import moeba.Runner;
import moeba.parameterization.ParameterizationExercise;
import moeba.parameterization.problem.ParameterizationProblem;

public class HVProblem extends ParameterizationProblem {
    private String outputFolder;
    private String[] prefixes;

    public HVProblem(ParameterizationExercise parameterizationExercise, String staticConf, String[] prefixes, String outputFolder) {
        super(parameterizationExercise, staticConf);
        this.prefixes = prefixes;
        this.outputFolder = outputFolder;
    }

    @Override
    public CompositeSolution evaluate(CompositeSolution solution) {

        // Get solution id
        int cnt = super.parallelCount.incrementAndGet();
        String solutionOutputFolder = outputFolder + "/HV-" + String.format("%03d", cnt) + "/";

        // Get arguments
        String solutionArgs = super.getArgsFromSolution(solution);
        String strArgs = staticConf + " " + solutionArgs;
        String[] args = super.preprocessArguments(strArgs.split(" "));

        // Get number of objectives
        int numObjectives = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--str-fitness-functions=")) {
                numObjectives = args[i].split(";").length;
                break;
            }
        }

        // Create reference point
        double[] referencePoint = new double[numObjectives];
        for (int i = 0; i < numObjectives; i++) {
            referencePoint[i] = 1.0;
        }

        // Set reference point to HV metric
        PISAHypervolume hv = new PISAHypervolume(referencePoint);

        // Run MOEBA algorithm for each benchmark
        double score = 0.0;
        for (int i = 0; i < prefixes.length; i++) {

            // Config problem
            String benchSolOutputFolder = solutionOutputFolder + "/bench-" + i + "/";
            ArrayList<String> listArgs = new ArrayList<>(Arrays.asList(args));
            listArgs.add("--input-dataset=" + prefixes[i] + "-data.csv");
            listArgs.add("--input-column-types=" + prefixes[i] + "-types.json");
            listArgs.add("--output-folder=" + benchSolOutputFolder);
            String[] benchArgs = listArgs.toArray(new String[0]);

            // Run MOEBA algorithm
            Runner.main(benchArgs);

            // Get the front
            String funFile = benchSolOutputFolder + "/FUN.csv";
            double[][] front = super.readVectors(funFile, ",");

            score += -1 * hv.compute(front);
        }
        
        // Evaluate solution
        solution.objectives()[0] = score / prefixes.length;

        // Delete solution output folder
        try {
            FileUtils.deleteDirectory(new File(solutionOutputFolder));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return solution;
    }
    
}
