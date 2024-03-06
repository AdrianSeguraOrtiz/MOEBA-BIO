package moeba.operator.crossover.biclustersbinary.impl;

import java.util.BitSet;
import java.util.Random;

import moeba.operator.crossover.biclustersbinary.BiclusterBinaryCrossover;

public class BicUniformCrossover implements BiclusterBinaryCrossover {
    private Random random;

    public BicUniformCrossover() {
        this.random = new Random();
    }

    /**
     * Uniform crossover operator for binary biclusters.
     * Each bit is selected at random to be inherited from one of the parents.
     *
     * @param s1 first parent
     * @param s2 second parent
     */
    @Override
    public void execute(BitSet s1, BitSet s2) {
        // Loop through each bit in the parent biclusters
        for (int i = 0; i < s1.length(); i++) {
            if (random.nextBoolean()) {
                s1.set(i, s2.get(i));
                s2.set(i, s1.get(i));
            }
        }
    }

}
