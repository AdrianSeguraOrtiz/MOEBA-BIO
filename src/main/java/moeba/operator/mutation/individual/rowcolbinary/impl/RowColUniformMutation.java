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
        for (int i = 0; i < bs.getBinarySetLength(); i++) {
            if (random.nextFloat() < mutationProbability) {
                bs.set(i, !bs.get(i));
            }
        }
    }
}
