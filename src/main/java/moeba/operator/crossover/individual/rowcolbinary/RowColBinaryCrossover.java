package moeba.operator.crossover.individual.rowcolbinary;

import org.uma.jmetal.util.binarySet.BinarySet;

public interface RowColBinaryCrossover {
    
    public void execute(BinarySet bs1, BinarySet bs2);

}
