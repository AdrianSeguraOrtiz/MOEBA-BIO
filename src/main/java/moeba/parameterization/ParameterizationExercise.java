package moeba.parameterization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

public class ParameterizationExercise {

    public List<String> doubleNames;
    public List<String> integerNames;
    public List<Bounds<Double>> doubleBounds;
    public List<Bounds<Integer>> integerBounds;
    public List<ValueFunc<Double>> doubleFuncs;
    public List<ValueFunc<Integer>> integerFuncs;
    public int evaluations;
    public int populationSize;
    public int numThreads;

    public interface ValueFunc<T> {
        String getValue(T value);
    }

    public ParameterizationExercise (File confFile, int evaluations, int populationSize, int numThreads) {
        this.evaluations = evaluations;
        this.populationSize = populationSize;
        this.numThreads = numThreads;

        this.doubleNames = new ArrayList<>();
        this.integerNames = new ArrayList<>();
        this.doubleBounds = new ArrayList<>();
        this.integerBounds = new ArrayList<>();
        this.doubleFuncs = new ArrayList<>();
        this.integerFuncs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(confFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values[1].equalsIgnoreCase("string")) {
                    String[] options = values[2].split("\\|");
                    this.integerNames.add(values[0]);
                    this.integerBounds.add(Bounds.create(0, options.length - 1));
                    this.integerFuncs.add((i) -> options[i]);
                } else if (values[1].equalsIgnoreCase("string-comb")) {
                    String[] options = values[2].split("\\|");
                    for (int i = 0; i < options.length; i++) {
                        String currentOption = options[i];
                        this.integerNames.add("--comb" + values[0] + "--" + i);
                        this.integerBounds.add(Bounds.create(0, 1));
                        this.integerFuncs.add((j) -> j == 0 ? "''" : currentOption);
                    }
                } else if (values[1].equalsIgnoreCase("double")) {
                    String[] bounds = values[2].split("\\-");
                    this.doubleNames.add(values[0]);
                    this.doubleBounds.add(Bounds.create(Double.parseDouble(bounds[0]), Double.parseDouble(bounds[1])));
                    this.doubleFuncs.add((d) -> String.valueOf(d));
                } else if (values[1].equalsIgnoreCase("integer")) {
                    String[] bounds = values[2].split("\\-");
                    int step = Integer.parseInt(bounds[2]);
                    this.integerNames.add(values[0]);
                    this.integerBounds.add(Bounds.create(Integer.parseInt(bounds[0]) / step, Integer.parseInt(bounds[1]) / step));
                    this.integerFuncs.add((i) -> String.valueOf(i * step));
                } else {
                    throw new IllegalArgumentException("Unsupported type: " + values[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the values of all arguments from the given solution.
     *
     * @param solution The solution containing the variables.
     * @return The values of all arguments as a string, separated by spaces.
     */
    public String getArgsFromSolution(ParameterizationSolution solution) {
        // Initialize an empty string to store the arguments
        String res = "";

        // Get the double and integer solutions from the given solution
        DoubleSolution doubleSolution = (DoubleSolution) solution.variables().get(0);
        IntegerSolution integerSolution = (IntegerSolution) solution.variables().get(1);

        // Iterate over the double variables and add their names and values to the result string
        for (int i = 0; i < doubleSolution.variables().size(); i++) {
            res += this.doubleNames.get(i) + "=";
            res += this.doubleFuncs.get(i).getValue(doubleSolution.variables().get(i)) + " ";
        }

        // Iterate over the integer variables and add their names and values to the result string
        for (int i = 0; i < integerSolution.variables().size(); i++) {
            res += this.integerNames.get(i) + "=";
            res += this.integerFuncs.get(i).getValue(integerSolution.variables().get(i)) + " ";
        }

        // Remove the trailing space and return the result string
        return res.substring(0, res.length() - 1);
    }


    /**
     * Retrieves the value of a specific argument from the given solution.
     *
     * @param arg The name of the argument to retrieve the value of.
     * @param solution The solution containing the variables.
     * @return The value of the argument, or null if the argument is not found.
     */
    public String getValueOfArg(String arg, ParameterizationSolution solution) {
        // Iterate over the double names and check if the argument matches
        for (int i = 0; i < this.doubleNames.size(); i++) {
            if (this.doubleNames.get(i).equals(arg)) {
                // If it matches, retrieve the value using the double function
                return this.doubleFuncs.get(i).getValue((double) solution.variables().get(0).variables().get(i));
            }
        }

        // Iterate over the integer names and check if the argument matches
        for (int i = 0; i < this.integerNames.size(); i++) {    
            if (this.integerNames.get(i).equals(arg)) {
                // If it matches, retrieve the value using the integer function
                return this.integerFuncs.get(i).getValue((int) solution.variables().get(1).variables().get(i));
            }
        }

        // If the argument is not found, return null
        return null;
    }
    
}
