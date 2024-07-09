package moeba.parameterization.problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.doublesolution.impl.DefaultDoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;

import moeba.parameterization.ParameterizationExercise;
import moeba.parameterization.ParameterizationSolution;
import moeba.utils.observer.ProblemObserver.ObserverInterface;

public abstract class ParameterizationProblem implements Problem<ParameterizationSolution> {
    protected ParameterizationExercise parameterizationExercise;
    protected String staticConf;
    protected AtomicInteger parallelCount;
    protected ObserverInterface[] observers;

    public ParameterizationProblem(ParameterizationExercise parameterizationExercise, String staticConf, ObserverInterface[] observers) {
        this.parameterizationExercise = parameterizationExercise;
        this.staticConf = staticConf;
        this.parallelCount = new AtomicInteger();
        this.observers = observers;
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
    public ParameterizationSolution createSolution() {
        DoubleSolution doubleSolution = new DefaultDoubleSolution(getNumberOfObjectives(), getNumberOfConstraints(), this.parameterizationExercise.doubleBounds);
        IntegerSolution integerSolution = new DefaultIntegerSolution(getNumberOfObjectives(), getNumberOfConstraints(), this.parameterizationExercise.integerBounds);

        return new ParameterizationSolution(Arrays.asList(doubleSolution, integerSolution));
    }

    /**
     * Preprocesses the given command line arguments, handling the case where an argument is a sub-argument.
     *
     * @param args the command line arguments to be preprocessed
     * @return an array of preprocessed arguments
     */
    public static String[] preprocessArguments(String[] args) {
        // Map to store the values of sub-arguments
        Map<String, Map<String, Map<String, String>>> subArguments = new HashMap<>();

        // Map to store the values of comb-arguments
        Map<String, String> combArguments = new HashMap<>();
        
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
            } else if (arg.startsWith("--comb")) {
                // Process comb-arguments
                String[] parts = arg.split("--");
                String mainArg = "--" + parts[2];
                String combValue = parts[3].split("=")[1];
                
                // Add to the map of comb-arguments
                if (!combValue.equals("''")) {
                    String value = combArguments.containsKey(mainArg) ? combArguments.get(mainArg) + ";" + combValue : combValue;
                    combArguments.put(mainArg, value);
                }

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

        // Add comb-arguments values as a main argument
        combArguments.forEach((key, value) -> finalArguments.add(key + "=" + value));
        
        // Return the final processed arguments
        return finalArguments.toArray(new String[0]);
    }

    public ParameterizationExercise getParameterizationExercise() {
        return parameterizationExercise;
    }

    public void registerInfo(ParameterizationSolution solution) {
        // Notify all registered observers with the evaluation result
        for (ObserverInterface observer : observers) {
            observer.register(solution);
        }
    }
}
