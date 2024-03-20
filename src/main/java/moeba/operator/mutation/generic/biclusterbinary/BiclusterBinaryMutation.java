package moeba.operator.mutation.generic.biclusterbinary;

import org.uma.jmetal.util.binarySet.BinarySet;

public interface BiclusterBinaryMutation {

    public void execute(BinarySet bs, double mutationProbability);
    
}
