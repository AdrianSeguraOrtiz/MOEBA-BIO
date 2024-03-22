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
        // Loop through each bit in the parent cells
        boolean aux;
        for (int i = 0; i < s1.getBinarySetLength(); i++) {
            if (random.nextBoolean()) {
                aux = s1.get(i);
                s1.set(i, s2.get(i));
                s2.set(i, aux);
            }
        }
    }

}
