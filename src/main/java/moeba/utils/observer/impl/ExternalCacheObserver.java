package moeba.utils.observer.impl;

import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import moeba.utils.observer.ProblemObserver.ObserverInterface;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

public class ExternalCacheObserver implements ObserverInterface {
    private int populationSize;
    private Map<String, Double> cache;
    private AtomicInteger parallelCount;
    private ArrayList<Integer> generationCacheCalls;

    public ExternalCacheObserver(int populationSize, Map<String, Double> cache) {
        this.populationSize = populationSize;
        this.cache = cache;
        this.parallelCount = new AtomicInteger();
        this.generationCacheCalls = new ArrayList<>();
    }

    @Override
    public void register(CompositeSolution result) {
        int cnt = this.parallelCount.incrementAndGet();
        if (cnt % this.populationSize == 0) {
            this.generationCacheCalls.add(cnt - this.cache.size());
        }
    }

    @Override
    public void writeToFile(String strFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(strFile))) {
            String strVector = Arrays.toString(this.generationCacheCalls.toArray());
            bw.write(strVector.substring(1, strVector.length() - 1) + "\n");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
