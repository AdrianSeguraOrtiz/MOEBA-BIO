package moeba.parameterization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                    String[] options = getCombinations(values[2].split("\\|"));
                    this.integerNames.add(values[0]);
                    this.integerBounds.add(Bounds.create(0, options.length - 1));
                    this.integerFuncs.add((i) -> options[i]);
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
     * Generates all possible combinations of the input strings.
     *
     * @param input The input strings to generate combinations from.
     * @return An array of strings representing all possible combinations.
     */
    private static String[] getCombinations(String[] input) {
        // List to store the generated combinations
        List<String> result = new ArrayList<>();

        // Recursively generate combinations and add them to the result list
        generateCombinations(input, 0, new ArrayList<>(), result);

        // Convert the list to an array and return it
        return result.toArray(new String[result.size()]);
    }

    /**
     * Recursively generates all possible combinations of elements from the input array.
     *
     * @param input     The input array of elements.
     * @param index     The current index being processed.
     * @param current   The current combination being built.
     * @param result    The list to store all generated combinations.
     */
    private static void generateCombinations(String[] input, int index, List<String> current, List<String> result) {
        // Base case: all elements processed
        if (index == input.length) {
            if (current.size() > 1) {
                // Add the combination to the result list
                result.add(String.join(";", current));
            }
            return;
        }

        // Exclude current element
        generateCombinations(input, index + 1, current, result);

        // Include current element
        current.add(input[index]);
        generateCombinations(input, index + 1, current, result);
        current.remove(current.size() - 1);
    }
    
}
