package moeba.utils.observer.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import moeba.utils.observer.ProblemObserver.ObserverInterface;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.binarySet.BinarySet;

/**
 * Observes the number of biclusters identified in each generation of an evolutionary algorithm.
 * It tracks the count and calculates the percentage of occurrences of each bicluster size across generations.
 */
public class BiclusterCountObserver implements ObserverInterface {
    // Object for thread-safe synchronization
    private final Object lockObject = new Object();
    // Population size of each generation
    private int populationSize;
    // Number of generations to observe
    private int numGenerations;
    // Counter for individuals processed
    private int cnt;
    // Map to store counts of biclusters
    private Map<Integer, Integer> biclusterCounts;
    // Map to store percentages of biclusters across generations
    private Map<Integer, double[]> biclusterPercentages;

    /**
     * Constructs a BiclusterCountObserver with specified population size and number of generations.
     * @param populationSize The size of the population in the evolutionary algorithm.
     * @param numGenerations The total number of generations to be observed.
     */
    public BiclusterCountObserver(int populationSize, int numGenerations) {
        this.populationSize = populationSize;
        this.numGenerations = numGenerations;
        this.cnt = 0;
        this.biclusterCounts = new HashMap<>();
        this.biclusterPercentages = new HashMap<>();
    }

    /**
     * Registers a solution to update bicluster count and calculate percentages.
     * This method is called whenever a solution is evaluated.
     * @param result The CompositeSolution instance that represents a solution in the population.
     */
    @Override
    public void register(CompositeSolution result) {
        synchronized(lockObject) {
            // Extracts the number of biclusters from the binary part of the solution
            int numBiclusters = ((BinarySet) result.variables().get(1).variables().get(0)).cardinality() + 1;
            // Updates the count of biclusters
            biclusterCounts.merge(numBiclusters, 1, Integer::sum);
            // Initializes the percentage array if not already done
            biclusterPercentages.putIfAbsent(numBiclusters, new double[numGenerations + 5]);

            cnt++;
            // At the end of each generation, calculate percentages and reset counts
            if (cnt % populationSize == 0) {
                biclusterCounts.forEach((size, count) -> {
                    double percentage = (double) count / this.populationSize * 100;
                    biclusterPercentages.get(size)[cnt / this.populationSize - 1] = percentage;
                });

                biclusterCounts.clear();
            }
        }
    }

    /**
     * Writes the observed bicluster counts and percentages to a specified file.
     * @param strFile The filename to which the data should be written.
     */
    @Override
    public void writeToFile(String strFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(strFile))) {
            // Iterate over each bicluster size and its percentage array
            for (Map.Entry<Integer, double[]> entry : biclusterPercentages.entrySet()) {
                int biclusterCount = entry.getKey();
                bw.write(biclusterCount + ", ");

                double[] percentages = entry.getValue();
                int limit = this.cnt / this.populationSize;
                // Write percentages for each generation
                for (int i = 1; i < limit; i++) {
                    bw.write(String.format("%.2f", percentages[i]) + (i == limit - 1 ? "" : ", "));
                }
                bw.write("\n");
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
