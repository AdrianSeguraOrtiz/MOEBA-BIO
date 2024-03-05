package moeba.operator.crossover.biclustersbinary;

import java.util.BitSet;

public interface BiclusterBinaryCrossover {

    public BitSet[] execute(BitSet s1, BitSet s2);
}
