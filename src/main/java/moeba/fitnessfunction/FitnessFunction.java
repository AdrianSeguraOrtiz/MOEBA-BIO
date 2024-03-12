package moeba.fitnessfunction;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FitnessFunction {
    protected Object[][] data;
    protected Class<?>[] types;
    protected ConcurrentHashMap<String, Double> internalCache;
    private RunnableFunc func;

    public interface RunnableFunc {
        double run(ArrayList<ArrayList<Integer>[]> biclusters);
    }

    public FitnessFunction(Object[][] data, Class<?>[] types, ConcurrentHashMap<String, Double> internalCache) {
        this.data = data;
        this.types = types;
        this.internalCache = internalCache;
        this.func = internalCache == null ? this::runWithoutCache : this::runWithCache;
    }

    protected abstract double runWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters);
    protected abstract double runWithCache(ArrayList<ArrayList<Integer>[]> biclusters);

    public double run(ArrayList<ArrayList<Integer>[]> biclusters) {
        return func.run(biclusters);
    }

    protected abstract double getBiclusterScore(ArrayList<Integer>[] bicluster);
}
