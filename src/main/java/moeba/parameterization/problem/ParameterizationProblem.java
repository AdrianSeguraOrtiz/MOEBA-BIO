package moeba.parameterization.problem;

import java.util.Arrays;

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

    public ParameterizationProblem(ParameterizationExercise parameterizationExercise, String staticConf) {
        this.parameterizationExercise = parameterizationExercise;
        this.staticConf = staticConf;
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

    protected String getArgsFromSolution(CompositeSolution solution) {
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

        return res;
    }

    public ParameterizationExercise getParameterizationExercise() {
        return parameterizationExercise;
    }
}
