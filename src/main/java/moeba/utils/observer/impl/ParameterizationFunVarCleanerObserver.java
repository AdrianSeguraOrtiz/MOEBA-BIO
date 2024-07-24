package moeba.utils.observer.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.AtomicDoubleArray;
import moeba.parameterization.ParameterizationExercise;
import moeba.parameterization.ParameterizationSolution;
import moeba.parameterization.problem.ParameterizationProblem;
import moeba.utils.observer.ProblemObserver.ObserverInterface;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.doublesolution.impl.DefaultDoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

public class ParameterizationFunVarCleanerObserver implements ObserverInterface {
    private ParameterizationExercise pe;
    private Map<String, String[][]> values;
    private int numObjectives;
    private AtomicDoubleArray progressiveValues;
    private AtomicInteger parallelCount;

    public ParameterizationFunVarCleanerObserver(ParameterizationExercise pe, int numObjectives) {
        this.pe = pe;
        this.values = new HashMap<>();
        this.numObjectives = numObjectives;
        this.progressiveValues = new AtomicDoubleArray(numObjectives);
        this.parallelCount = new AtomicInteger();
        this.initialize();
    }

    @Override
    public void register(CompositeSolution result) {

        ParameterizationSolution solution = new ParameterizationSolution(result);
        String[] args = ParameterizationProblem.preprocessArguments(this.pe.getArgsFromSolution(solution).split(" "));
        int cnt = this.parallelCount.incrementAndGet();

        int gen = (cnt - 1) / this.pe.populationSize;
        int pos = (cnt - 1) % this.pe.populationSize;
        for (String arg : args) {
            String[] mainKV = arg.split("=", 2);
            String[] vComps = mainKV[1].split(";");
            String processedVComps = "";
            for (String vComp : vComps) {
                String[] subArgs = vComp.split("[()=, ]");
                processedVComps += subArgs[0] + ";";
                for (int i = 1; i < subArgs.length-1; i+=2) {
                    this.values.get("--" + subArgs[0] + "-" + subArgs[i])[gen][pos] = subArgs[i + 1];
                }
            }
            this.values.get(mainKV[0])[gen][pos] = processedVComps.substring(0, processedVComps.length() - 1);
        }

        boolean save = false;
        for (int i = 0; i < this.numObjectives; i++) {
            this.values.get("Objective - " + i)[gen][pos] = String.valueOf(result.objectives()[i]);
            double currentMin = this.progressiveValues.get(i);
            if (result.objectives()[i] < currentMin) {
                this.progressiveValues.compareAndSet(i, currentMin, result.objectives()[i]);
                save = true;
            }
        }
        if (!save) {
            // Clean the individual's metadata in case it does not surpass the best recorded so far.
            result = new ParameterizationSolution(result);
        }
    }

    @Override
    public void writeToFile(String strFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(strFile)))) {
            for (Map.Entry<String, String[][]> entryParamArray : this.values.entrySet()) {
                bw.write(entryParamArray.getKey() + ": {");
                String[][] generations = entryParamArray.getValue();
                for (int i = 0; i < generations.length; i++) {
                    bw.write(Arrays.toString(generations[i]));
                    if (i < generations.length - 1) {
                        bw.write(", ");
                    }
                }
                bw.write("}\n");
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private void initialize() {
        DoubleSolution doubleExample = new DefaultDoubleSolution(1, pe.doubleBounds);
        IntegerSolution integerExample = new DefaultIntegerSolution(1, pe.integerBounds);
        ParameterizationSolution example = new ParameterizationSolution(Arrays.asList(doubleExample, integerExample));

        String allOptions = "";
        for (int i = 0; i < example.variables().get(0).variables().size(); i++) {
            allOptions += pe.getArgOfValue(i, pe.doubleNames, pe.doubleFuncs, pe.doubleBounds.get(i).getLowerBound()) + " ";
        }
        for (int i = 0; i < example.variables().get(1).variables().size(); i++) {
            for (int j = pe.integerBounds.get(i).getLowerBound(); j <= pe.integerBounds.get(i).getUpperBound(); j++) {
                allOptions += pe.getArgOfValue(i, pe.integerNames, pe.integerFuncs, j) + " ";
            }
        }
        String[] args = ParameterizationProblem.preprocessArguments(allOptions.split(" "));

        for (String arg : args) {
            String[] mainKV = arg.split("=", 2);
            String[] vComps = mainKV[1].split(";");
            for (String vComp : vComps) {
                String[] subArgs = vComp.split("[()=, ]");
                for (int i = 1; i < subArgs.length-1; i+=2) {
                    this.values.put("--" + subArgs[0] + "-" + subArgs[i], new String[pe.evaluations / pe.populationSize + 5][pe.populationSize]);
                }
            }
            this.values.put(mainKV[0], new String[pe.evaluations / pe.populationSize + 5][pe.populationSize]);
        }

        for(int i = 0; i < numObjectives; i++) {
            this.progressiveValues.set(i, Double.MAX_VALUE);
            this.values.put("Objective - " + i, new String[pe.evaluations / pe.populationSize + 5][pe.populationSize]);
        }
        
    }
    
}
