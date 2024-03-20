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
        for (int i = 0; i < bs.getBinarySetLength(); i++) {
            if (random.nextFloat() < mutationProbability) {
                bs.set(i, !bs.get(i));
            }
        }
    }
    
}
