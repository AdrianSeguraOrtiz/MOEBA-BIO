package moeba.operator.mutation.individual.rowcolbinary.impl;

import java.util.Random;

import moeba.operator.mutation.individual.rowcolbinary.RowColBinaryMutation;
import org.uma.jmetal.util.binarySet.BinarySet;

public class RowColUniformMutation implements RowColBinaryMutation {
    
    private Random random;

    public RowColUniformMutation() {
        this.random = new Random();
    }

    public RowColUniformMutation(Random random) {
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
