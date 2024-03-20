package moeba.operator.mutation.generic.cellbinary;

import org.uma.jmetal.util.binarySet.BinarySet;

public interface CellBinaryMutation {
    
    public void execute(BinarySet bs, double mutationProbability);
    
}
