package moeba.operator.mutation.generic.cellbinary.impl;

import org.uma.jmetal.util.binarySet.BinarySet;
import java.util.Random;

import moeba.operator.mutation.generic.cellbinary.CellBinaryMutation;

public class CellUniformMutation implements CellBinaryMutation {

    private Random random;

    public CellUniformMutation() {
        this.random = new Random();
    }

    public CellUniformMutation(Random random) {
        this.random = random;
    }

    @Override
    public void execute(BinarySet bs, double mutationProbability) {
        int numBits = bs.getBinarySetLength();
        int numBitsChanged = (int)((mutationProbability-0.05 + 0.1*random.nextFloat())*numBits);
        int index;
        for (int i = 0; i < numBitsChanged; i++) {
            index = random.nextInt(numBits);
            bs.set(index, !bs.get(index));
        }
    }
    
}
