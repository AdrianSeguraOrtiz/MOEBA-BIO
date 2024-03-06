package moeba;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import moeba.fitnessfunction.FitnessFunction;
import moeba.problem.AbstractMixedIntegerBinaryProblem;

public class Problem extends AbstractMixedIntegerBinaryProblem {

    private Object [][] data;
    private Class<?> [] types;
    protected FitnessFunction[] fitnessFunctions;
    private Representation representation;

    // Generic Representation
    public Problem(Object[][] data, Class<?> [] types, String strFitnessFunctions) {
        super(data.length, 1 + data[0].length, 0, data.length - 1, data.length);
        representation = Representation.GENERIC;
        initialize(data, types, strFitnessFunctions, -1);
    }

    // Specific Representation
    public Problem(Object[][] data, Class<?> [] types, String strFitnessFunctions, int numBiclusters) {
        super(data.length, numBiclusters, 0, numBiclusters - 1, data[0].length);
        if (numBiclusters < 2 || numBiclusters >= data.length) {
            throw new IllegalArgumentException("The number of biclusters must be between 2 and " + (data.length - 1) + ".");
        }
        representation = Representation.SPECIFIC;
        initialize(data, types, strFitnessFunctions, numBiclusters);
    }

    @Override
    public CompositeSolution createSolution() {
        IntegerSolution integerSolution = new DefaultIntegerSolution(getNumberOfObjectives(), getNumberOfConstraints(), super.integerBounds);
        BinarySolution binarySolution = new DefaultBinarySolution(super.numBitsPerVariable, getNumberOfObjectives());

        if (representation == Representation.GENERIC) {
            List<Integer> rowIndexes = IntStream.rangeClosed(0, data.length - 1).boxed().collect(Collectors.toList());
            Collections.shuffle(rowIndexes);
            for (int i = 0; i < data.length; i++) {
                integerSolution.variables().set(i, rowIndexes.get(i));
            }
        }

        return new CompositeSolution(Arrays.asList(integerSolution, binarySolution));
    }
    
    // Evaluate an individual
    @Override
    public CompositeSolution evaluate(CompositeSolution solution) {
        ArrayList<ArrayList<Integer>[]> biclusters = StaticUtils.getBiclustersFromRepresentation(solution, representation, data.length, data[0].length);
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
        setNumberOfVariables((representation == Representation.GENERIC) ? (numRows + 1 + data[0].length) : (numRows + numBiclusters));
        setNumberOfObjectives(this.fitnessFunctions.length);
        setName("Problem");
    }

}
