package moeba.operator.crossover.rowpermutation.impl;

import moeba.operator.crossover.rowpermutation.RowPermutationCrossover;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.Random;
import java.util.Arrays;

public class PartiallyMappedCrossover implements RowPermutationCrossover {
    private Random random;

    public PartiallyMappedCrossover() {
        this.random = new Random();
    }

    public PartiallyMappedCrossover(Random random) {
        this.random = random;
    }

    @Override
    public void execute(IntegerSolution s1, IntegerSolution s2) {
        int n = s1.variables().size();

        // select cutting points
		int cuttingPoint1 = random.nextInt(n);
		int cuttingPoint2 = random.nextInt(n - 1);

		if (cuttingPoint1 == cuttingPoint2) {
			cuttingPoint2 = n - 1;
		} else if (cuttingPoint1 > cuttingPoint2) {
			int swap = cuttingPoint1;
			cuttingPoint1 = cuttingPoint2;
			cuttingPoint2 = swap;
		}

        // exchange between the cutting points, setting up replacement arrays
		int[] replacement1 = new int[n];
		int[] replacement2 = new int[n];

		Arrays.fill(replacement1, -1);
		Arrays.fill(replacement2, -1);

        int v1, v2;
        for (int i = cuttingPoint1; i <= cuttingPoint2; i++) {
            v1 = s1.variables().get(i);
            v2 = s2.variables().get(i);

			s1.variables().set(i, v2);
            s2.variables().set(i, v1);

			replacement1[v2] = v1;
			replacement2[v1] = v2;
		}

		// fill in remaining slots with replacements
		for (int i = 0; i < n; i++) {
			if ((i < cuttingPoint1) || (i > cuttingPoint2)) {
				int n1 = s1.variables().get(i);
				int m1 = replacement1[n1];

				int n2 = s2.variables().get(i);
				int m2 = replacement2[n2];

				while (m1 != -1) {
					n1 = m1;
					m1 = replacement1[m1];
				}

				while (m2 != -1) {
					n2 = m2;
					m2 = replacement2[m2];
				}

				s1.variables().set(i, n1);
				s2.variables().set(i, n2);
			}
		}

    }
    
}
