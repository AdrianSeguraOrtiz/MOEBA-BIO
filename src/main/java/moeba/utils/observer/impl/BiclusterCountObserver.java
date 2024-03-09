package moeba.utils.observer.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import moeba.utils.observer.ProblemObserver.ObserverInterface;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.binarySet.BinarySet;

public class BiclusterCountObserver implements ObserverInterface {
    private final Object lockObject = new Object();
    private int populationSize;
    private int numGenerations;
    private int cnt;
    private Map<Integer, Integer> biclusterCounts;
    private Map<Integer, double[]> biclusterPercentages;

    public BiclusterCountObserver(int populationSize, int numGenerations) {
        this.populationSize = populationSize;
        this.numGenerations = numGenerations;
        this.cnt = 0;
        this.biclusterCounts = new HashMap<>();
        this.biclusterPercentages = new HashMap<>();
    }

    @Override
    public void register(CompositeSolution result) {
        synchronized(lockObject) {
            int numBiclusters = ((BinarySet) result.variables().get(1).variables().get(0)).cardinality() + 1;
            biclusterCounts.merge(numBiclusters, 1, Integer::sum);
            biclusterPercentages.putIfAbsent(numBiclusters, new double[numGenerations + 5]);

            cnt++;
            if (cnt % populationSize == 0) {
                biclusterCounts.forEach((size, count) -> {
                    double percentage = (double) count / this.populationSize * 100;
                    biclusterPercentages.get(size)[cnt / this.populationSize - 1] = percentage;
                });

                biclusterCounts.clear();
            }
        }
    }

    @Override
    public void writeToFile(String strFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(strFile))) {
            for (Map.Entry<Integer, double[]> entry : biclusterPercentages.entrySet()) {
                int biclusterCount = entry.getKey();
                bw.write(biclusterCount + ", ");

                double[] percentages = entry.getValue();
                int limit = this.cnt / this.populationSize;
                for (int i = 0; i < limit; i++) {
                    bw.write(String.format("%.2f", percentages[i]) + (i == limit - 1 ? "" : ", "));
                }
                bw.write("\n");
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
