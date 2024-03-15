package moeba.operator.crossover.rowbiclustermixed;

import java.util.BitSet;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

public interface RowBiclusterMixedCrossover {

    public void execute(IntegerSolution is1, IntegerSolution is2, BitSet bs1, BitSet bs2);
}
