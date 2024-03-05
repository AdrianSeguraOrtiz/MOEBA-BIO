package moeba.operator.crossover.cellbinary.impl;

import java.util.BitSet;
import java.util.Random;

import moeba.operator.crossover.cellbinary.CellBinaryCrossover;

public class CellUniformCrossover implements CellBinaryCrossover {
    private Random random;

    public CellUniformCrossover() {
        this.random = new Random();
    }

    /**
     * Uniform crossover operator for binary cells.
     * Each bit is selected at random to be inherited from one of the parents.
     *
     * @param s1 first parent
     * @param s2 second parent
     * @return offspring as a pair of {@link BitSet}s
     */
    @Override
    public BitSet[] execute(BitSet s1, BitSet s2) {
        // Use bit length of one of the parents, as they are of the same length
        int length = s1.length();
        BitSet offspring1 = new BitSet(length);
        BitSet offspring2 = new BitSet(length);

        // Loop through each bit in the parent cells
        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                offspring1.set(i, s1.get(i));
                offspring2.set(i, s2.get(i));
            } else {
                offspring1.set(i, s2.get(i));
                offspring2.set(i, s1.get(i));
            }
        }

        // Return the offspring
        return new BitSet[]{offspring1, offspring2};
    }

}
