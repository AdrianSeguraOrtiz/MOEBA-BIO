package moeba.operator.crossover.rowpermutation.impl;

import moeba.operator.crossover.rowpermutation.RowPermutationCrossover;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import java.util.*;

public class EdgeRecombinationCrossover implements RowPermutationCrossover {

    private Random random1;
    private Random random2;

    public EdgeRecombinationCrossover() {
        this.random1 = new Random();
        this.random2 = new Random();
    }

    public EdgeRecombinationCrossover(Random random1, Random random2) {
        this.random1 = random1;
        this.random2 = random2;
    }

    @Override
    public void execute(IntegerSolution s1, IntegerSolution s2) {
        // Generar el primer hijo a partir de los padres
        List<Integer> offspring1 = generateOffspring(s1, s2);
        // Generar el segundo hijo intercambiando los roles de los padres
        List<Integer> offspring2 = generateOffspring(s2, s1);

        // Actualizar los padres con la información de los descendientes
        for (int i = 0; i < s1.variables().size(); i++) {
            s1.variables().set(i, offspring1.get(i));
            s2.variables().set(i, offspring2.get(i));
        }
    }
    
    private List<Integer> generateOffspring(IntegerSolution parent1, IntegerSolution parent2) {
        int size = parent1.variables().size();
        Map<Integer, Set<Integer>> edgeMap = buildEdgeMap(parent1, parent2, size);
        Integer current = selectStartingElement(edgeMap);
        List<Integer> offspring = new ArrayList<>();
        offspring.add(current);
        updateEdgeMap(current, edgeMap);

        while (offspring.size() < size) {
            current = selectNextElement(current, edgeMap);
            offspring.add(current);
            updateEdgeMap(current, edgeMap);
        }
        return offspring;
    }

    private Map<Integer, Set<Integer>> buildEdgeMap(IntegerSolution s1, IntegerSolution s2, int size) {
        Map<Integer, Set<Integer>> edgeMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            int element1 = s1.variables().get(i);
            edgeMap.computeIfAbsent(element1, k -> new HashSet<>()).add(s1.variables().get((i - 1 + size) % size));
            edgeMap.computeIfAbsent(element1, k -> new HashSet<>()).add(s1.variables().get((i + 1) % size));

            int element2 = s2.variables().get(i);
            edgeMap.computeIfAbsent(element2, k -> new HashSet<>()).add(s2.variables().get((i - 1 + size) % size));
            edgeMap.computeIfAbsent(element2, k -> new HashSet<>()).add(s2.variables().get((i + 1) % size));
        }
        edgeMap.forEach((key, value) -> value.remove(key)); // Eliminar auto-referencias
        return edgeMap;
    }

    private Integer selectStartingElement(Map<Integer, Set<Integer>> edgeMap) {
        return new ArrayList<>(edgeMap.keySet()).get(random1.nextInt(edgeMap.size()));
    }

    private void updateEdgeMap(Integer current, Map<Integer, Set<Integer>> edgeMap) {
        edgeMap.forEach((key, value) -> value.remove(current));
        edgeMap.remove(current);
    }

    private Integer selectNextElement(Integer current, Map<Integer, Set<Integer>> edgeMap) {
        Set<Integer> edges = edgeMap.get(current);
        if (edges == null || edges.isEmpty()) {
            return new ArrayList<>(edgeMap.keySet()).get(random2.nextInt(edgeMap.size()));
        }
        return Collections.min(edges, Comparator.comparingInt(e -> edgeMap.getOrDefault(e, Collections.emptySet()).size()));
    }
}
