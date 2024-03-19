package moeba.operator.crossover.generic.rowpermutation.impl;

import org.uma.jmetal.solution.integersolution.IntegerSolution;
import java.util.*;

import moeba.operator.crossover.generic.rowpermutation.RowPermutationCrossover;

/**
 * Implements the edge recombination crossover algorithm for row permutation crossovers.
 * This crossover technique is especially effective for genetic algorithms dealing with
 * pathfinding or sequencing problems, ensuring a high-quality mix of parent genes
 * to produce offspring that inherit edges (connections) from both parents.
 */
public class EdgeRecombinationCrossover implements RowPermutationCrossover {

    private Random random1;
    private Random random2;

    /**
     * Default constructor initializing random generators for selecting starting points
     * and next elements during the crossover process.
     */
    public EdgeRecombinationCrossover() {
        this.random1 = new Random();
        this.random2 = new Random();
    }

    /**
     * Constructor allowing for custom random generators, facilitating controlled
     * randomness in testing or specific runtime conditions.
     *
     * @param random1 Random generator for initial element selection.
     * @param random2 Random generator for subsequent element selections.
     */
    public EdgeRecombinationCrossover(Random random1, Random random2) {
        this.random1 = random1;
        this.random2 = random2;
    }

    /**
     * Executes the edge recombination crossover on two parent solutions,
     * generating two offspring by recombining the parents' edges.
     *
     * @param s1 The first parent solution.
     * @param s2 The second parent solution.
     */
    @Override
    public void execute(IntegerSolution s1, IntegerSolution s2) {
        // Generate the first offspring from the parents
        List<Integer> offspring1 = generateOffspring(s1, s2);
        // Generate the second offspring by swapping the roles of the parents
        List<Integer> offspring2 = generateOffspring(s2, s1);

        // Update the parents with the offspring's data
        for (int i = 0; i < s1.variables().size(); i++) {
            s1.variables().set(i, offspring1.get(i));
            s2.variables().set(i, offspring2.get(i));
        }
    }
    
    /**
     * Generates an offspring from two parents by using the edge recombination method.
     * This method builds an edge map representing connections in both parents and
     * produces an offspring that inherits these connections.
     *
     * @param parent1 The first parent solution.
     * @param parent2 The second parent solution.
     * @return A list representing the offspring's sequence of values.
     */
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

    /**
     * Builds an edge map from two parent solutions, mapping each element to its neighboring
     * elements, to represent possible transitions.
     *
     * @param s1 The first parent solution.
     * @param s2 The second parent solution.
     * @param size The size of the parent solutions.
     * @return A map representing the edge connections between elements.
     */
    private Map<Integer, Set<Integer>> buildEdgeMap(IntegerSolution s1, IntegerSolution s2, int size) {
        Map<Integer, Set<Integer>> edgeMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            int element1 = s1.variables().get(i);
            // Add edges from both previous and next elements in the sequence
            edgeMap.computeIfAbsent(element1, k -> new HashSet<>()).add(s1.variables().get((i - 1 + size) % size));
            edgeMap.computeIfAbsent(element1, k -> new HashSet<>()).add(s1.variables().get((i + 1) % size));

            int element2 = s2.variables().get(i);
            edgeMap.computeIfAbsent(element2, k -> new HashSet<>()).add(s2.variables().get((i - 1 + size) % size));
            edgeMap.computeIfAbsent(element2, k -> new HashSet<>()).add(s2.variables().get((i + 1) % size));
        }
        // Remove self-references to clean up the edge map
        edgeMap.forEach((key, value) -> value.remove(key));
        return edgeMap;
    }

    /**
     * Selects a random starting element from the edge map to begin offspring generation.
     *
     * @param edgeMap The edge map built from parent solutions.
     * @return A starting element for the offspring generation process.
     */
    private Integer selectStartingElement(Map<Integer, Set<Integer>> edgeMap) {
        return new ArrayList<>(edgeMap.keySet()).get(random1.nextInt(edgeMap.size()));
    }

    /**
     * Updates the edge map by removing the current element and its references from all edge sets.
     *
     * @param current The current element being processed.
     * @param edgeMap The current edge map.
     */
    private void updateEdgeMap(Integer current, Map<Integer, Set<Integer>> edgeMap) {
        edgeMap.forEach((key, value) -> value.remove(current));
        edgeMap.remove(current);
    }

    /**
     * Selects the next element based on the current element's edges, preferring elements with fewer edges.
     *
     * @param current The current element in the offspring generation process.
     * @param edgeMap The edge map with remaining connections.
     * @return The next element to add to the offspring.
     */
    private Integer selectNextElement(Integer current, Map<Integer, Set<Integer>> edgeMap) {
        Set<Integer> edges = edgeMap.get(current);
        // If no edges or the edge set is empty, pick a random element
        if (edges == null || edges.isEmpty()) {
            return new ArrayList<>(edgeMap.keySet()).get(random2.nextInt(edgeMap.size()));
        }
        // Prefer elements with fewer edges to maintain a more optimal path
        return Collections.min(edges, Comparator.comparingInt(e -> edgeMap.getOrDefault(e, Collections.emptySet()).size()));
    }
}
