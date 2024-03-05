package moeba.operator.crossover.cellbinary;

import java.util.BitSet;

public interface CellBinaryCrossover {
    
    public BitSet[] execute(BitSet s1, BitSet s2);
}
