package moeba.operator.crossover.generic.biclusterbinary.impl;

import org.uma.jmetal.util.binarySet.BinarySet;
import java.util.Random;

import moeba.operator.crossover.generic.biclusterbinary.BiclusterBinaryCrossover;

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
    public void execute(BinarySet s1, BinarySet s2) {
        int numBits = s1.getBinarySetLength();
        int numBitsChanged = (int)((0.45 + 0.1*random.nextFloat())*numBits);
        int index;
        boolean aux;
        for (int i = 0; i < numBitsChanged; i++) {
            index = random.nextInt(numBits);
            aux = s1.get(index);
            s1.set(index, s2.get(index));
            s2.set(index, aux);
        }
    }

}
