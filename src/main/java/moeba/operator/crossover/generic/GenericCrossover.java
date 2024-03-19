package moeba.operator.crossover.generic;

import java.util.List;
import java.util.ArrayList;

import moeba.operator.crossover.generic.biclustersbinary.BiclusterBinaryCrossover;
import moeba.operator.crossover.generic.cellbinary.CellBinaryCrossover;
import moeba.operator.crossover.generic.rowbiclustermixed.RowBiclusterMixedCrossover;
import moeba.operator.crossover.generic.rowpermutation.RowPermutationCrossover;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.errorchecking.Check;

public class GenericCrossover implements CrossoverOperator<CompositeSolution> {
    private double crossoverProbability;
    private RowPermutationCrossover rowPermutationCrossover;
    private BiclusterBinaryCrossover biclusterBinaryCrossover;
    private RowBiclusterMixedCrossover rowBiclusterMixedCrossover;
    private CellBinaryCrossover cellBinaryCrossover;
    private JMetalRandom random;  
    private RunnerGenericCrossover runnerGenericCrossover;

    public interface RunnerGenericCrossover {
        public void execute(IntegerSolution offSpring1IntSol, IntegerSolution offSpring2IntSol, BinarySolution offSpring1BinSol, BinarySolution offSpring2BinSol);
    }

    public GenericCrossover(double crossoverProbability, RowPermutationCrossover rowPermutationCrossover, BiclusterBinaryCrossover biclusterBinaryCrossover, CellBinaryCrossover cellBinaryCrossover) {
        this.crossoverProbability = crossoverProbability;
        this.rowPermutationCrossover = rowPermutationCrossover;
        this.biclusterBinaryCrossover = biclusterBinaryCrossover;
        this.cellBinaryCrossover = cellBinaryCrossover;
        this.random = JMetalRandom.getInstance();
        this.rowBiclusterMixedCrossover = null;
        this.runnerGenericCrossover = this::crossSep;
    }

    public GenericCrossover(double crossoverProbability, RowBiclusterMixedCrossover rowBiclusterMixedCrossover, CellBinaryCrossover cellBinaryCrossover) {
        this.crossoverProbability = crossoverProbability;
        this.rowBiclusterMixedCrossover = rowBiclusterMixedCrossover;
        this.cellBinaryCrossover = cellBinaryCrossover;
        this.random = JMetalRandom.getInstance();
        this.rowPermutationCrossover = null;
        this.biclusterBinaryCrossover = null;
        this.runnerGenericCrossover = this::crossTogether;
    }

    @Override
    public List<CompositeSolution> execute(List<CompositeSolution> source) {
        Check.notNull(source);
        Check.that(source.size() == 2, "There must be two parents instead of " + source.size());

        List<CompositeSolution> offspring = new ArrayList<>();

        CompositeSolution offSpring1 = (CompositeSolution) source.get(0).copy();
        IntegerSolution offSpring1IntSol = (IntegerSolution) offSpring1.variables().get(0);
        BinarySolution offSpring1BinSol = (BinarySolution) offSpring1.variables().get(1);

        CompositeSolution offSpring2 = (CompositeSolution) source.get(1).copy();
        IntegerSolution offSpring2IntSol = (IntegerSolution) offSpring2.variables().get(0);
        BinarySolution offSpring2BinSol = (BinarySolution) offSpring2.variables().get(1);

        if (random.nextDouble(0, 1) <= this.crossoverProbability) {
            runnerGenericCrossover.execute(offSpring1IntSol, offSpring2IntSol, offSpring1BinSol, offSpring2BinSol);
        } 

        offspring.add(offSpring1);
        offspring.add(offSpring2);
        return offspring;
    }

    public void crossSep(IntegerSolution offSpring1IntSol, IntegerSolution offSpring2IntSol, BinarySolution offSpring1BinSol, BinarySolution offSpring2BinSol) {
        // Rows permutation crossover
        rowPermutationCrossover.execute(offSpring1IntSol, offSpring2IntSol);
        // Biclusters binary crossover
        biclusterBinaryCrossover.execute(offSpring1BinSol.variables().get(0), offSpring2BinSol.variables().get(0));
        // Cells binary crossover
        for (int i = 1; i < offSpring1BinSol.variables().size(); i++) {
            cellBinaryCrossover.execute(offSpring1BinSol.variables().get(i), offSpring2BinSol.variables().get(i));
        }
    }

    public void crossTogether(IntegerSolution offSpring1IntSol, IntegerSolution offSpring2IntSol, BinarySolution offSpring1BinSol, BinarySolution offSpring2BinSol) {
        // Rows permutation crossover
        rowBiclusterMixedCrossover.execute(offSpring1IntSol, offSpring2IntSol, offSpring1BinSol.variables().get(0), offSpring2BinSol.variables().get(0));
        // Cells binary crossover
        for (int i = 1; i < offSpring1BinSol.variables().size(); i++) {
            cellBinaryCrossover.execute(offSpring1BinSol.variables().get(i), offSpring2BinSol.variables().get(i));
        }
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
