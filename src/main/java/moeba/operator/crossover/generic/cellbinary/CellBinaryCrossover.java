package moeba.operator.crossover.generic.cellbinary;

import org.uma.jmetal.util.binarySet.BinarySet;

public interface CellBinaryCrossover {
    
    public void execute(BinarySet s1, BinarySet s2);
}
