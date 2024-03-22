package moeba.operator.crossover.individual.rowcolbinary.impl;

import java.util.Random;

import moeba.operator.crossover.individual.rowcolbinary.RowColBinaryCrossover;
import org.uma.jmetal.util.binarySet.BinarySet;

public class RowColUniformCrossover implements RowColBinaryCrossover {
    private Random random;

    public RowColUniformCrossover() {
        this.random = new Random();
    }

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
