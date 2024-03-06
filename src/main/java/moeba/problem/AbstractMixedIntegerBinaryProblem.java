package moeba.problem;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.bounds.Bounds;

public abstract class AbstractMixedIntegerBinaryProblem implements Problem<CompositeSolution> {
    protected List<Bounds<Integer>> integerBounds;
    protected List<Integer> numBitsPerVariable;
    private String name;
    private int numberOfVariables;
    private int numberOfObjectives;
    private int numberOfConstraints;

    public AbstractMixedIntegerBinaryProblem (int numIntegerVar, int numBinaryVar, int lowerIntegerBound, int upperIntegerBound, int numBitsPerVariable) {
        this.integerBounds = new ArrayList<>(numIntegerVar);
        for (int i = 0; i < numIntegerVar; i++) {
            this.integerBounds.add(Bounds.create(lowerIntegerBound, upperIntegerBound));
        }
        this.numBitsPerVariable = new ArrayList<>(numBinaryVar);
        for (int i = 0; i < numBinaryVar; i++) {
            this.numBitsPerVariable.add(numBitsPerVariable);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNumberOfVariables(int numberOfVariables) {
        this.numberOfVariables = numberOfVariables;
    }

    public int getNumberOfVariables() {
        return numberOfVariables;
    }

    public void setNumberOfObjectives(int numberOfObjectives) {
        this.numberOfObjectives = numberOfObjectives;
    }

    public int getNumberOfObjectives() {
        return numberOfObjectives;
    }

    public void setNumberOfConstraints(int numberOfConstraints) {
        this.numberOfConstraints = numberOfConstraints;
    }

    public int getNumberOfConstraints() {
        return numberOfConstraints;
    }

}
