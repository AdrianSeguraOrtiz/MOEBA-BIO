package moeba.parameterization.problem.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.solution.compositesolution.CompositeSolution;

import moeba.Runner;
import moeba.parameterization.ParameterizationExercise;
import moeba.parameterization.problem.ParameterizationProblem;

public class HVProblem extends ParameterizationProblem {

    public HVProblem(ParameterizationExercise parameterizationExercise, String staticConf) {
        super(parameterizationExercise, staticConf);
    }

    @Override
    public CompositeSolution evaluate(CompositeSolution solution) {

        String solutionArgs = super.getArgsFromSolution(solution);
        String args = staticConf + " " + solutionArgs;

        Runner.main(HVProblem.preprocessArguments(args.split(" ")));

        // TODO: Evaluate solution
        solution.objectives()[0] = 1;

        return solution;
        
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
    
}
