package moeba.operator.mutation.individual.rowcolbinary;

import org.uma.jmetal.util.binarySet.BinarySet;

public interface RowColBinaryMutation {
    
    public void execute(BinarySet bs, double mutationProbability);

}
