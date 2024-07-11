package moeba.parameterization.problem.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.nio.file.Paths;

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

        // Executor service for parallel execution
        ExecutorService executorService = Executors.newFixedThreadPool(prefixes.length);
        List<Future<Double>> futures = new ArrayList<>();

        for (String prefix : prefixes) {
            Callable<Double> task = () -> {
                // Config problem
                ArrayList<String> listArgs = new ArrayList<>(Arrays.asList(args));
                listArgs.add("--input-dataset=" + prefix + "-data.csv");
                listArgs.add("--input-column-types=" + prefix + "-types.json");
                String[] benchArgs = listArgs.toArray(new String[0]);

                // Run MOEBA algorithm
                Runner runner = new Runner();
                CommandLine commandLine = new CommandLine(runner);
                commandLine.execute(benchArgs);

                // Get solutions
                List<CompositeSolution> benchSolutions = runner.getSolutions();

                if (benchSolutions.size() != 0) {
                    // Get front and store MOEBA solutions to HV individual
                    double[][] front = new double[benchSolutions.size()][numObjectives];
                    List<ParameterizationSolution> subPopulation = new ArrayList<>();
                    for (int j = 0; j < benchSolutions.size(); j++) {
                        ParameterizationSolution subSolution = new ParameterizationSolution(benchSolutions.get(j));
                        subPopulation.add(subSolution);
                        front[j] = subSolution.objectives();
                    }
                    synchronized (solution) {
                        solution.subPopulations.add(subPopulation);
                        solution.subObservers.add(runner.getObservers());
                        solution.tags.add(Paths.get(prefix).getFileName().toString());
                    }

                    // Compute HV
                    return -1 * hv.compute(front);
                }
                return 0.0;
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

        return solution;
    }

    
    @Override
    public String getName() {
        return "HyperVolume Problem (unsupervised)";
    }
    
}
