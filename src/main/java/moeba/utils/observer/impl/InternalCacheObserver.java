package moeba.utils.observer.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import moeba.utils.observer.ProblemObserver.ObserverInterface;
import moeba.utils.storage.CacheStorage;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

public class InternalCacheObserver implements ObserverInterface {
    private int populationSize;
    private String[] fitnessFunctions;
    private CacheStorage<String, Double>[] internalCaches;
    private AtomicInteger parallelCount;
    private ArrayList<Integer>[] generationCacheCalls;

    @SuppressWarnings("unchecked")
    public InternalCacheObserver(int populationSize, String[] fitnessFunctions, CacheStorage<String, Double>[] internalCaches) {
        this.populationSize = populationSize;
        this.fitnessFunctions = fitnessFunctions;
        this.internalCaches = internalCaches;
        this.parallelCount = new AtomicInteger();
        this.generationCacheCalls = new ArrayList[fitnessFunctions.length];
        for(int i = 0; i < fitnessFunctions.length; i++) {
            this.generationCacheCalls[i] = new ArrayList<>();
        }
    }

    @Override
    public void register(CompositeSolution result) {
        if (parallelCount.incrementAndGet() % this.populationSize == 0) {
            for(int i = 0; i < fitnessFunctions.length; i++) {
                this.generationCacheCalls[i].add(this.internalCaches[i].getNumGetters());
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
