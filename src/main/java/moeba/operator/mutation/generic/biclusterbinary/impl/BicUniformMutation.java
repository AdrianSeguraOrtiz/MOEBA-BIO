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
        for (int i = 0; i < bs.getBinarySetLength(); i++) {
            if (random.nextFloat() < mutationProbability) {
                boolean join = random.nextBoolean();
                int index = join ? bs.nextSetBit(i) : bs.nextClearBit(i);
                if (index == -1) index = join ? bs.previousSetBit(i) : bs.previousClearBit(i);
                if (index == -1) index = i;
                bs.set(index, !bs.get(index));
            }
        }
    }
    
}
