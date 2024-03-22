package moeba.operator.crossover.generic.cellbinary.impl;

import org.uma.jmetal.util.binarySet.BinarySet;
import java.util.Random;

import moeba.operator.crossover.generic.cellbinary.CellBinaryCrossover;

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
