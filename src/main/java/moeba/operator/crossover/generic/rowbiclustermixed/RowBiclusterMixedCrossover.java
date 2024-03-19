package moeba.operator.crossover.generic.rowbiclustermixed;

import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

public interface RowBiclusterMixedCrossover {

    public void execute(IntegerSolution is1, IntegerSolution is2, BinarySet bs1, BinarySet bs2);
}
