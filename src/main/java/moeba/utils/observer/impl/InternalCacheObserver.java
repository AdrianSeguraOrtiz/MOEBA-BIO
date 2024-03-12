package moeba.utils.observer.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

import moeba.utils.observer.ProblemObserver.ObserverInterface;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.binarySet.BinarySet;

public class InternalCacheObserver implements ObserverInterface {
    private int populationSize;
    private String[] fitnessFunctions;
    private ConcurrentHashMap<String, Double>[] internalCaches;
    private AtomicInteger parallelCount;
    private AtomicInteger parallelBicCount;
    private ArrayList<Integer>[] generationCacheCalls;

    @SuppressWarnings("unchecked")
    public InternalCacheObserver(int populationSize, String[] fitnessFunctions, ConcurrentHashMap<String, Double>[] internalCaches) {
        this.populationSize = populationSize;
        this.fitnessFunctions = fitnessFunctions;
        this.internalCaches = internalCaches;
        this.parallelCount = new AtomicInteger();
        this.parallelBicCount = new AtomicInteger();
        this.generationCacheCalls = new ArrayList[fitnessFunctions.length];
        for(int i = 0; i < fitnessFunctions.length; i++) {
            this.generationCacheCalls[i] = new ArrayList<>();
        }
    }

    @Override
    public void register(CompositeSolution result) {
        int numBiclusters = ((BinarySet) result.variables().get(1).variables().get(0)).cardinality() + 1;
        int cnt = parallelBicCount.addAndGet(numBiclusters);
        if (parallelCount.incrementAndGet() % this.populationSize == 0) {
            for(int i = 0; i < fitnessFunctions.length; i++) {
                this.generationCacheCalls[i].add(cnt - this.internalCaches[i].size());
            }
        }
    }

    @Override
    public void writeToFile(String strFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(strFile))) {
            for(int i = 0; i < fitnessFunctions.length; i++) {
                String strVector = Arrays.toString(this.generationCacheCalls[i].toArray());
                bw.write(this.fitnessFunctions[i] + ", " + strVector.substring(1, strVector.length() - 1) + "\n");
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
