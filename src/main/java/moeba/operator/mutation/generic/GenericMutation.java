package moeba.operator.mutation.generic;

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
    private RowPermutationMutation rowPermutationMutation;
    private BiclusterBinaryMutation biclusterBinaryMutation;
    private CellBinaryMutation cellBinaryMutation; 

    public GenericMutation(double mutationProbability, RowPermutationMutation rowPermutationMutation, BiclusterBinaryMutation biclusterBinaryMutation, CellBinaryMutation cellBinaryMutation) {
        this.mutationProbability = mutationProbability;
        this.rowPermutationMutation = rowPermutationMutation;
        this.biclusterBinaryMutation = biclusterBinaryMutation;
        this.cellBinaryMutation = cellBinaryMutation;
    }

    @Override
    public CompositeSolution execute(CompositeSolution solution) {
        Check.notNull(solution);

        IntegerSolution intSol = (IntegerSolution) solution.variables().get(0);
        BinarySolution binSol = (BinarySolution) solution.variables().get(1);

        rowPermutationMutation.execute(intSol);
        biclusterBinaryMutation.execute(binSol.variables().get(0));
        for (int i = 1; i < binSol.variables().size(); i++) {
            cellBinaryMutation.execute(binSol.variables().get(i));
        }

        return solution;
    }

    @Override
    public double getMutationProbability() {
        return mutationProbability;
    }
    
}
