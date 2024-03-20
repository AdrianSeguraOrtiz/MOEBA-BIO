package moeba.operator.mutation.generic;

import java.util.concurrent.atomic.AtomicInteger;

import moeba.operator.mutation.generic.biclusterbinary.BiclusterBinaryMutation;
import moeba.operator.mutation.generic.cellbinary.CellBinaryMutation;
import moeba.operator.mutation.generic.rowpermutation.RowPermutationMutation;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.errorchecking.Check;

public class GenericMutation implements MutationOperator<CompositeSolution> {
    private double mutationProbability;
    private double maxMutationProbability;
    private double minMutationProbability;
    private int numApproxMutations;
    private AtomicInteger numOperations;
    private RowPermutationMutation rowPermutationMutation;
    private BiclusterBinaryMutation biclusterBinaryMutation;
    private CellBinaryMutation cellBinaryMutation; 

    public GenericMutation(String mutationProbability, int numApproxMutations, RowPermutationMutation rowPermutationMutation, BiclusterBinaryMutation biclusterBinaryMutation, CellBinaryMutation cellBinaryMutation) {
        String[] parts = mutationProbability.split("-");
        if (parts.length == 1) {
            double p = Double.parseDouble(mutationProbability);
            this.maxMutationProbability = p;
            this.minMutationProbability = p;
        } else if (parts.length == 2) {
            this.maxMutationProbability = Double.parseDouble(parts[0]);
            this.minMutationProbability = Double.parseDouble(parts[1]);
        } else {
            throw new IllegalArgumentException("Invalid mutation probability: " + mutationProbability);
        }

        this.numApproxMutations = numApproxMutations;
        this.rowPermutationMutation = rowPermutationMutation;
        this.biclusterBinaryMutation = biclusterBinaryMutation;
        this.cellBinaryMutation = cellBinaryMutation;
        this.numOperations = new AtomicInteger();
    }

    @Override
    public CompositeSolution execute(CompositeSolution solution) {
        Check.notNull(solution);
        int doned = numOperations.incrementAndGet();
        this.mutationProbability = minMutationProbability + (maxMutationProbability - minMutationProbability) * (numApproxMutations - doned) / numApproxMutations;

        IntegerSolution intSol = (IntegerSolution) solution.variables().get(0);
        BinarySolution binSol = (BinarySolution) solution.variables().get(1);

        rowPermutationMutation.execute(intSol, mutationProbability);
        biclusterBinaryMutation.execute(binSol.variables().get(0), mutationProbability);
        for (int i = 1; i < binSol.variables().size(); i++) {
            cellBinaryMutation.execute(binSol.variables().get(i), mutationProbability);
        }

        return solution;
    }

    @Override
    public double getMutationProbability() {
        return mutationProbability;
    }
    
}
