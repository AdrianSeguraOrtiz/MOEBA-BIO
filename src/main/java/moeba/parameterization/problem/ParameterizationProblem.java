package moeba.parameterization.problem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.doublesolution.impl.DefaultDoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

import moeba.parameterization.ParameterizationExercise;

public abstract class ParameterizationProblem implements Problem<CompositeSolution> {
    protected ParameterizationExercise parameterizationExercise;
    protected String staticConf;
    protected AtomicInteger parallelCount;

    public ParameterizationProblem(ParameterizationExercise parameterizationExercise, String staticConf) {
        this.parameterizationExercise = parameterizationExercise;
        this.staticConf = staticConf;
        this.parallelCount = new AtomicInteger();
    }

    @Override
    public int getNumberOfVariables() {
        return 2;
    }

    @Override
    public int getNumberOfObjectives() {
        return 1;
    }

    @Override
    public int getNumberOfConstraints() {
        return 0;
    }

    @Override
    public String getName() {
        return "ParameterizationProblem";
    }

    @Override
    public CompositeSolution createSolution() {
        DoubleSolution doubleSolution = new DefaultDoubleSolution(getNumberOfObjectives(), getNumberOfConstraints(), this.parameterizationExercise.doubleBounds);
        IntegerSolution integerSolution = new DefaultIntegerSolution(getNumberOfObjectives(), getNumberOfConstraints(), this.parameterizationExercise.integerBounds);

        return new CompositeSolution(Arrays.asList(doubleSolution, integerSolution));
    }

    public String getArgsFromSolution(CompositeSolution solution) {
        String res = "";

        DoubleSolution doubleSolution = (DoubleSolution) solution.variables().get(0);
        IntegerSolution integerSolution = (IntegerSolution) solution.variables().get(1);

        for (int i = 0; i < doubleSolution.variables().size(); i++) {
            res += this.parameterizationExercise.doubleNames.get(i) + "=";
            res += this.parameterizationExercise.doubleFuncs.get(i).getValue(doubleSolution.variables().get(i)) + " ";
        }
        for (int i = 0; i < integerSolution.variables().size(); i++) {
            res += this.parameterizationExercise.integerNames.get(i) + "=";
            res += this.parameterizationExercise.integerFuncs.get(i).getValue(integerSolution.variables().get(i)) + " ";
        }

        return res.substring(0, res.length() - 1);
    }

    /**
     * Preprocesses the given command line arguments, handling the case where an argument is a sub-argument.
     *
     * @param args the command line arguments to be preprocessed
     * @return an array of preprocessed arguments
     */
    protected String[] preprocessArguments(String[] args) {
        // Map to store the values of sub-arguments
        Map<String, Map<String, Map<String, String>>> subArguments = new HashMap<>();
        
        // List to store the final processed arguments
        List<String> finalArguments = new ArrayList<>();
        
        // Process each argument
        for (String arg : args) {
            // If the argument is a sub-argument
            if (arg.startsWith("--sub")) {
                // Process sub-arguments
                String[] parts = arg.split("--");
                String mainArg = "--" + parts[2];
                String subString = parts[3];
                String[] subParam = parts[4].split("=");
                String subKey = subParam[0];
                String subValue = subParam[1];
                
                // Add to the map of sub-arguments
                subArguments.putIfAbsent(mainArg, new HashMap<>());
                subArguments.get(mainArg).putIfAbsent(subString, new HashMap<>());
                subArguments.get(mainArg).get(subString).put(subKey, subValue);
            } else {
                // Add main argument to the final arguments list
                finalArguments.add(arg);
            }
        }
        
        // Inject sub-argument values into main arguments
        for (int i = 0; i < finalArguments.size(); i++) {
            String arg = finalArguments.get(i);
            String[] parts = arg.split("=");
            if (parts.length == 2 && subArguments.containsKey(parts[0])) {
                String mainArg = parts[0];
                String value = parts[1];
                StringBuilder newValue = new StringBuilder();
                
                // Process each sub-string and its sub-arguments
                for (String subString : value.split(";")) {
                    newValue.append(subString);
                    if (subArguments.get(mainArg).containsKey(subString)) {
                        newValue.append("(");
                        Map<String, String> subArgsMap = subArguments.get(mainArg).get(subString);
                        subArgsMap.forEach((key, subValue) -> {
                            newValue.append(key).append("=").append(subValue).append(",");
                        });
                        newValue.setLength(newValue.length() - 1); // Remove the last comma
                        newValue.append(")");
                    }
                    newValue.append(";");
                }
                newValue.setLength(newValue.length() - 1); // Remove the last semicolon
                
                // Update the main argument in the final arguments list
                finalArguments.set(i, mainArg + "=" + newValue.toString());
            }
        }
        
        // Return the final processed arguments
        return finalArguments.toArray(new String[0]);
    }

    /**
     * Reads vectors from a file and returns them as a two-dimensional array of doubles.
     *
     * @param filePath The path to the file containing the vectors.
     * @param separator The separator used to split the string representation of the vectors.
     * @return A two-dimensional array of doubles representing the vectors.
     */
    protected double[][] readVectors(String filePath, String separator) {
        // Array to store the vectors
        double[][] referenceVectors;

        // Read all lines from the file
        List<String> vectorStrList = new ArrayList<>();
        try {
            vectorStrList = Files.readAllLines(Paths.get(filePath));
            vectorStrList.remove(0); // Remove header
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Allocate memory for the array based on the number of vectors
        referenceVectors = new double[vectorStrList.size()][];

        // Iterate over each vector string and parse it into doubles
        for (int i = 0; i < vectorStrList.size(); i++) {
            // Get the string representation of the vector
            String vectorStr = vectorStrList.get(i);

            // Split the string into objects
            String[] objectArray = vectorStr.split(separator);

            // Create a new array to store the vector
            referenceVectors[i] = new double[objectArray.length];

            // Iterate over each object and parse it into a double
            for (int j = 0; j < objectArray.length; j++) {
                // Parse the object into a double and store it in the array
                referenceVectors[i][j] = Double.parseDouble(objectArray[j]);
            }
        }

        // Return the array of vectors
        return referenceVectors;
    }

    public ParameterizationExercise getParameterizationExercise() {
        return parameterizationExercise;
    }
}
