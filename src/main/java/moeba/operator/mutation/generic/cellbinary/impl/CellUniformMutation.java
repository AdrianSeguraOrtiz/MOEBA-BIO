package moeba.operator.mutation.generic.cellbinary.impl;

import org.uma.jmetal.util.binarySet.BinarySet;
import java.util.Random;

import moeba.operator.mutation.generic.cellbinary.CellBinaryMutation;

public class CellUniformMutation implements CellBinaryMutation {

    private float probability;
    private Random random;

    public CellUniformMutation(float probability) {
        this.probability = probability;
        this.random = new Random();
    }

    public CellUniformMutation(float probability, Random random) {
        this.probability = probability;
        this.random = random;
    }

    @Override
    public void execute(BinarySet bs) {
        for (int i = 0; i < bs.getBinarySetLength(); i++) {
            if (random.nextFloat() < probability) {
                bs.set(i, !bs.get(i));
            }
        }
    }
    
}
