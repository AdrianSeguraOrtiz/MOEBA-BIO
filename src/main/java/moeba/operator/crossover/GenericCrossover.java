package moeba.operator.crossover;

import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import moeba.operator.crossover.biclustersbinary.BiclusterBinaryCrossover;
import moeba.operator.crossover.cellbinary.CellBinaryCrossover;
import moeba.operator.crossover.rowpermutation.RowPermutationCrossover;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.errorchecking.Check;

public class GenericCrossover implements CrossoverOperator<IntegerSolution> {
    private double crossoverProbability;
    private int numRows;
    private RowPermutationCrossover rowPermutationCrossover;
    private BiclusterBinaryCrossover biclusterBinaryCrossover;
    private CellBinaryCrossover cellBinaryCrossover;
    private JMetalRandom random;  

    public GenericCrossover(double crossoverProbability, int numRows, RowPermutationCrossover rowPermutationCrossover, BiclusterBinaryCrossover biclusterBinaryCrossover, CellBinaryCrossover cellBinaryCrossover) {
        this.crossoverProbability = crossoverProbability;
        this.numRows = numRows;
        this.rowPermutationCrossover = rowPermutationCrossover;
        this.biclusterBinaryCrossover = biclusterBinaryCrossover;
        this.cellBinaryCrossover = cellBinaryCrossover;
        this.random = JMetalRandom.getInstance();
    }

    @Override
    public List<IntegerSolution> execute(List<IntegerSolution> source) {
        Check.notNull(source);
        Check.that(source.size() == 2, "There must be two parents instead of " + source.size());

        List<IntegerSolution> offspring = new ArrayList<>();
        IntegerSolution offSpring1 = (IntegerSolution) source.get(0).copy();
        IntegerSolution offSpring2 = (IntegerSolution) source.get(1).copy();
        if (random.nextDouble(0, 1) <= this.crossoverProbability) {
            
            // Rows permutation crossover
            int[] parent1RowPerm = new int[numRows];
            int[] parent2RowPerm = new int[numRows];
            for (int i = 0; i < numRows; i++) {
                parent1RowPerm[i] = source.get(0).variables().get(i);
                parent2RowPerm[i] = source.get(1).variables().get(i);
            }
            int[][] offspringRowPerm = rowPermutationCrossover.execute(parent1RowPerm, parent2RowPerm);
            for (int i = 0; i < numRows; i++) {
                offSpring1.variables().set(i, offspringRowPerm[0][i]);
                offSpring2.variables().set(i, offspringRowPerm[1][i]);
            }

            // Biclusters binary crossover
            BitSet parent1BicBits = new BitSet(numRows);
            BitSet parent2BicBits = new BitSet(numRows);
            for (int i = 0; i < numRows; i++) {
                parent1BicBits.set(i, source.get(0).variables().get(i + numRows) == 1);
                parent2BicBits.set(i, source.get(1).variables().get(i + numRows) == 1);
            }
            BitSet[] offspringBicBits = biclusterBinaryCrossover.execute(parent1BicBits, parent2BicBits);
            for (int i = 0; i < numRows; i++) {
                offSpring1.variables().set(i + numRows, offspringBicBits[0].get(i) ? 1 : 0);
                offSpring2.variables().set(i + numRows, offspringBicBits[1].get(i) ? 1 : 0);
            }

            // Cells binary crossover
            BitSet parent1CellBits = new BitSet(numRows);
            BitSet parent2CellBits = new BitSet(numRows);
            for (int i = 0; i < numRows; i++) {
                parent1CellBits.set(i, source.get(0).variables().get(i + 2 * numRows) == 1);
                parent2CellBits.set(i, source.get(1).variables().get(i + 2 * numRows) == 1);
            }
            BitSet[] offspringCellBits = cellBinaryCrossover.execute(parent1CellBits, parent2CellBits);
            for (int i = 0; i < numRows; i++) {
                offSpring1.variables().set(i + 2 * numRows, offspringCellBits[0].get(i) ? 1 : 0);
                offSpring2.variables().set(i + 2 * numRows, offspringCellBits[1].get(i) ? 1 : 0);
            }
        } 

        offspring.add(offSpring1);
        offspring.add(offSpring2);
        return offspring;
    }

    @Override
    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    @Override
    public int getNumberOfRequiredParents() {
        return 2;
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return 2;
    }
    
}
