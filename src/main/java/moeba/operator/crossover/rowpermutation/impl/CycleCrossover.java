package moeba.operator.crossover.rowpermutation.impl;

import moeba.operator.crossover.rowpermutation.RowPermutationCrossover;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

import java.util.HashMap;
import java.util.Map;

public class CycleCrossover implements RowPermutationCrossover {

    @Override
    public void execute(IntegerSolution parent1, IntegerSolution parent2) {
        int length = parent1.variables().size();
    
        // Crear mapas de valor a índice para ambos padres
        Map<Integer, Integer> indexMapParent1 = new HashMap<>(length);
        Map<Integer, Integer> indexMapParent2 = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            indexMapParent1.put(parent1.variables().get(i), i);
            indexMapParent2.put(parent2.variables().get(i), i);
        }

        boolean[] visited = new boolean[length];

        for (int start = 0; start < length; start++) {
            if (!visited[start]) {
                int index = start;
                do {
                    visited[index] = true;
                    int itemInParent2 = parent2.variables().get(index);
                    index = indexMapParent1.get(itemInParent2);
                } while (index != start);

                // Realizar el intercambio de valores entre los padres basado en el ciclo identificado
                for (int i = 0; i < length; i++) {
                    if (visited[i]) {
                        int temp = parent1.variables().get(i);
                        parent1.variables().set(i, parent2.variables().get(i));
                        parent2.variables().set(i, temp);
                    }
                }
            }
        }
    }
}
