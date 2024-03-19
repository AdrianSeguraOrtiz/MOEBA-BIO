package moeba.utils.observer.impl;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import moeba.utils.observer.ProblemObserver.ObserverInterface;
import moeba.utils.storage.CacheStorage;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

public class ExternalCacheObserver implements ObserverInterface {
    private int populationSize;
    private CacheStorage<String, Double[]> externalCache;
    private AtomicInteger parallelCount;
    private ArrayList<Integer> generationCacheCalls;

    public ExternalCacheObserver(int populationSize, CacheStorage<String, Double[]> externalCache) {
        this.populationSize = populationSize;
        this.externalCache = externalCache;
        this.parallelCount = new AtomicInteger();
        this.generationCacheCalls = new ArrayList<>();
    }

    @Override
    public void register(CompositeSolution result) {
        if (this.parallelCount.incrementAndGet() % this.populationSize == 0) {
            this.generationCacheCalls.add(externalCache.getNumGetters());
        }
    }

    @Override
    public void writeToFile(String strFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(strFile))) {
            String strVector = this.generationCacheCalls.toString();
            bw.write(strVector.substring(1, strVector.length() - 1) + "\n");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
