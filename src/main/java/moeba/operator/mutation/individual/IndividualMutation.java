package moeba.operator.mutation.individual;

import java.util.concurrent.atomic.AtomicInteger;

import moeba.operator.mutation.individual.rowcolbinary.RowColBinaryMutation;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.errorchecking.Check;

public class IndividualMutation implements MutationOperator<CompositeSolution> {

    private double mutationProbability;
    private double maxMutationProbability;
    private double minMutationProbability;
    private int numApproxMutations;
    private AtomicInteger numOperations;
    private RowColBinaryMutation rowColBinaryMutation;
    
    public IndividualMutation(String mutationProbability, int numApproxMutations, RowColBinaryMutation rowColBinaryMutation) {
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
        this.rowColBinaryMutation = rowColBinaryMutation;
        this.numOperations = new AtomicInteger();
    }

    @Override
    public CompositeSolution execute(CompositeSolution solution) {
        Check.notNull(solution);
        int doned = numOperations.incrementAndGet();
        this.mutationProbability = minMutationProbability + (maxMutationProbability - minMutationProbability) * (numApproxMutations - doned) / numApproxMutations;

        BinarySet bs = (BinarySet) solution.variables().get(1).variables().get(0);
        rowColBinaryMutation.execute(bs, mutationProbability);

        return solution;
    }

    @Override
    public double getMutationProbability() {
        return mutationProbability;
    }
    
}
