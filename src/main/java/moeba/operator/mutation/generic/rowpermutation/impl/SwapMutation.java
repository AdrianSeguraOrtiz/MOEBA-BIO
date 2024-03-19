package moeba.operator.mutation.generic.rowpermutation.impl;

import java.util.Random;

import moeba.operator.mutation.generic.rowpermutation.RowPermutationMutation;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

public class SwapMutation implements RowPermutationMutation {

    private float probability;
    private Random random;

    public SwapMutation(float probability) {
        this.probability = probability;
        this.random = new Random();
    }

    public SwapMutation(float probability, Random random) {
        this.probability = probability;
        this.random = random;
    }

    @Override
    public void execute(IntegerSolution s) {
        for (int i = 0; i < s.variables().size(); i++) {
            if (random.nextFloat() < probability) {
                int index = random.nextInt(s.variables().size());
                int aux = s.variables().get(i);
                s.variables().set(i, s.variables().get(index));
                s.variables().set(index, aux);
            }
        }
    }
    
}
