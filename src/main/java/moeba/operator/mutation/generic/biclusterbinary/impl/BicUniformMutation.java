package moeba.operator.mutation.generic.biclusterbinary.impl;

import org.uma.jmetal.util.binarySet.BinarySet;
import java.util.Random;

import moeba.operator.mutation.generic.biclusterbinary.BiclusterBinaryMutation;

public class BicUniformMutation implements BiclusterBinaryMutation {
    
    private Random random;
    
    public BicUniformMutation() {
        this.random = new Random();
    }

    public BicUniformMutation(Random random) {
        this.random = random;
    }

    @Override
    public void execute(BinarySet bs, double mutationProbability) {
        int numBits = bs.getBinarySetLength();
        int numBitsChanged = (int)((mutationProbability-0.05 + 0.1*random.nextFloat())*numBits);
        int index;
        for (int i = 0; i < numBitsChanged; i++) {
            index = random.nextInt(numBits);
            boolean join = random.nextBoolean();
            int targetIndex = join ? bs.nextSetBit(index) : bs.nextClearBit(index);
            if (targetIndex == -1) targetIndex = join ? bs.previousSetBit(index) : bs.previousClearBit(index);
            if (targetIndex == -1) targetIndex = index;
            bs.set(targetIndex, !bs.get(targetIndex));
        }
    }
    
}
