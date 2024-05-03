package moeba.fitnessfunction;

import java.util.ArrayList;

import moeba.utils.storage.CacheStorage;

public abstract class FitnessFunction {
    protected double[][] data;
    protected Class<?>[] types;
    protected CacheStorage<String, Double> internalCache;
    protected RunnableFunc func;

    public interface RunnableFunc {
        double run(ArrayList<ArrayList<Integer>[]> biclusters);
    }

    public FitnessFunction(double[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache) {
        this.data = data;
        this.types = types;
        this.internalCache = internalCache;
    }

    public abstract double run(ArrayList<ArrayList<Integer>[]> biclusters);
    
}
