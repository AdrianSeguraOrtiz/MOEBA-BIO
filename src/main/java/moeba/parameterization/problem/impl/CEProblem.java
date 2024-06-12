package moeba.parameterization.problem.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.uma.jmetal.solution.compositesolution.CompositeSolution;

import moeba.StaticUtils.AlgorithmResult;
import moeba.parameterization.ParameterizationExercise;
import moeba.parameterization.ParameterizationRunner;
import moeba.parameterization.problem.ParameterizationProblem;

public class CEProblem extends ParameterizationProblem {

    private String[] prefixes;
    private ParameterizationExercise subExercise;
    private AtomicInteger parallelCount;

    public CEProblem(ParameterizationExercise parameterizationExercise, String staticConf, String[] prefixes, ParameterizationExercise subExercise) {
        super(parameterizationExercise, staticConf);

        this.prefixes = prefixes;
        this.subExercise = subExercise;
        this.parallelCount = new AtomicInteger();
    }

    @Override
    public CompositeSolution evaluate(CompositeSolution solution) {
        
        String solutionArgs = super.getArgsFromSolution(solution);
        String tmpFolder = "./tmp/" + this.hashCode() + "/";

        for (int i = 0; i < prefixes.length; i++) {
            String dataset = prefixes[i] + "-data.csv";
            String types = prefixes[i] + "-types.json";
            String outputFolder = tmpFolder + "/bench-" + i + "/";
            HVProblem hvproblem = new HVProblem(
                this.subExercise, 
                staticConf + " " + solutionArgs + "--input-dataset=" + dataset + " --input-column-types=" + types + " --output-folder=" + outputFolder
            );

            AlgorithmResult result = ParameterizationRunner.executeParameterizationAlgorithm(hvproblem);

        }

        // TODO: Evaluate solution
        solution.objectives()[0] = 1;

        // Remove tmp folder
        /**
        try {
            FileUtils.deleteDirectory(new File(tmpFolder));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        int cnt = this.parallelCount.incrementAndGet();
        if (cnt % parameterizationExercise.populationSize == 0) {
            System.out.println(cnt);
        }

        return solution;
    }
    
}
