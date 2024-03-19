package moeba.operator.mutation.generic.cellbinary.impl;

import org.uma.jmetal.util.binarySet.BinarySet;
import java.util.Random;

import moeba.operator.mutation.generic.cellbinary.CellBinaryMutation;

public class CellUniformMutation implements CellBinaryMutation {

    private double mutationProbability;
    private Random random;

    public CellUniformMutation(double mutationProbability) {
        this.mutationProbability = mutationProbability;
        this.random = new Random();
    }

    public CellUniformMutation(double mutationProbability, Random random) {
        this.mutationProbability = mutationProbability;
        this.random = random;
    }

    @Override
    public void execute(BinarySet bs) {
        for (int i = 0; i < bs.getBinarySetLength(); i++) {
            if (random.nextFloat() < mutationProbability) {
                bs.set(i, !bs.get(i));
            }
        }
    }
    
}
