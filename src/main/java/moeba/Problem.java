package moeba;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.uma.jmetal.problem.integerproblem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import moeba.fitnessfunction.FitnessFunction;

public class Problem extends AbstractIntegerProblem {

    private Object [][] data;
    private Class<?> [] types;
    protected FitnessFunction[] fitnessFunctions;
    private Representation representation;

    // Generic Representation
    public Problem(Object[][] data, Class<?> [] types, String strFitnessFunctions) {
        representation = Representation.GENERIC;
        initialize(data, types, strFitnessFunctions, -1);
    }

    // Specific Representation
    public Problem(Object[][] data, Class<?> [] types, String strFitnessFunctions, int numBiclusters) {
        if (numBiclusters < 2 || numBiclusters >= data.length) {
            throw new IllegalArgumentException("The number of biclusters must be between 2 and " + (data.length - 1) + ".");
        }
        representation = Representation.SPECIFIC;
        initialize(data, types, strFitnessFunctions, numBiclusters);
    }

    @Override
    public IntegerSolution createSolution() {
        IntegerSolution solution = new DefaultIntegerSolution(getNumberOfObjectives(), getBoundsForVariables());
        
        if (representation == Representation.GENERIC) {
            List<Integer> rowIndexes = IntStream.rangeClosed(1, data.length).boxed().collect(Collectors.toList());
            Collections.shuffle(rowIndexes);
            for (int i = 0; i < data.length; i++) {
                solution.variables().set(i, rowIndexes.get(i));
            }
        }
        
        return solution;
    }
    
    // Evaluate an individual
    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        
        Integer[] x = new Integer[getNumberOfVariables()];
        for (int i = 0; i < getNumberOfVariables(); i++) {
            x[i] = solution.variables().get(i);
        }

        Integer[][][] biclusters = StaticUtils.getBiclustersFromRepresentation(x, representation);
        for (int i = 0; i < fitnessFunctions.length; i++){
            solution.objectives()[i] = fitnessFunctions[i].run(biclusters);
        }

        return solution;
    }

    // Common initialization for both representations
    private void initialize(Object[][] data, Class<?> [] types, String strFitnessFunctions, int numBiclusters) {
        this.data = data;
        this.types = types;
        
        // Parse fitness functions
        String [] arrayStrFitnessFunctions = strFitnessFunctions.split(";");
        this.fitnessFunctions = new FitnessFunction[arrayStrFitnessFunctions.length];
        for (int i = 0; i < arrayStrFitnessFunctions.length; i++) {
            this.fitnessFunctions[i] = StaticUtils.getFitnessFunctionFromString(arrayStrFitnessFunctions[i], data, types);
        }

        // Configure jMetal Problem
        int numRows = data.length;
        setNumberOfVariables((representation == Representation.GENERIC) ? (2 * numRows + numRows * data[0].length) : (numRows + numRows * numBiclusters));
        setNumberOfObjectives(this.fitnessFunctions.length);
        setName("Problem");

        // Set bounds for selected representation
        List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables());
        List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables());
        for (int i = 0; i < numRows; i++) {
            lowerLimit.add(0);
            upperLimit.add((representation == Representation.GENERIC) ? numRows - 1 : numBiclusters - 1);
        }
        for (int i = numRows; i < getNumberOfVariables(); i++) {
            lowerLimit.add(0);
            upperLimit.add(1);
        }
        setVariableBounds(lowerLimit, upperLimit);
    }

}
