package moeba.parameterization.problem.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.experimental.qualityIndicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

import moeba.Runner;
import moeba.parameterization.ParameterizationExercise;
import moeba.parameterization.ParameterizationSolution;
import moeba.parameterization.problem.ParameterizationProblem;
import moeba.utils.observer.ProblemObserver.ObserverInterface;
import picocli.CommandLine;

public class HVProblem extends ParameterizationProblem {
    private String[] prefixes;
    private int numObjectives;

    public HVProblem(ParameterizationExercise parameterizationExercise, String staticConf, ObserverInterface[] observers, String[] prefixes, int numObjectives) {
        super(parameterizationExercise, staticConf, observers);
        this.prefixes = prefixes;
        this.numObjectives = numObjectives;
    }

    @Override
    public ParameterizationSolution evaluate(ParameterizationSolution solution) {

        // Get arguments
        String solutionArgs = this.parameterizationExercise.getArgsFromSolution(solution);
        String strArgs = staticConf + " " + solutionArgs;
        String[] args = super.preprocessArguments(strArgs.split(" "));

        // Create reference point
        double[] referencePoint = new double[this.numObjectives];
        for (int i = 0; i < this.numObjectives; i++) {
            referencePoint[i] = 1.0;
        }

        // Set reference point to HV metric
        PISAHypervolume hv = new PISAHypervolume(referencePoint);

        // Run MOEBA algorithm for each benchmark
        double score = 0.0;
        for (int i = 0; i < prefixes.length; i++) {

            // Config problem
            ArrayList<String> listArgs = new ArrayList<>(Arrays.asList(args));
            listArgs.add("--input-dataset=" + prefixes[i] + "-data.csv");
            listArgs.add("--input-column-types=" + prefixes[i] + "-types.json");
            String[] benchArgs = listArgs.toArray(new String[0]);

            // Run MOEBA algorithm
            Runner runner = new Runner();
            CommandLine commandLine = new CommandLine(runner);
            commandLine.execute(benchArgs);

            // Get solutions
            List<CompositeSolution> benchSolutions = runner.getSolutions();

            if (benchSolutions.size() != 0) {
                // Get front and store MOEBA solutions to HV individual
                double[][] front = new double[benchSolutions.size()][this.numObjectives];
                List<ParameterizationSolution> subPopulation = new ArrayList<>();
                for (int j = 0; j < benchSolutions.size(); j++) {
                    ParameterizationSolution subSolution = new ParameterizationSolution(benchSolutions.get(j));
                    subPopulation.add(subSolution);
                    front[j] = subSolution.objectives();
                }
                solution.subPopulations.add(subPopulation);
                solution.subObservers.add(runner.getObservers());

                // Compute HV
                score += -1 * hv.compute(front);
            }
        }
        
        // Evaluate solution
        solution.objectives()[0] = score / prefixes.length;

        // Observers
        this.registerInfo(solution);

        return solution;
    }
    
    @Override
    public String getName() {
        return "HyperVolume Problem (unsupervised)";
    }
    
}
