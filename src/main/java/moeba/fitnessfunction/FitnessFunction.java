package moeba.fitnessfunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import moeba.utils.storage.CacheStorage;

public abstract class FitnessFunction {
    protected String[][] data;
    protected Class<?>[] types;
    protected CacheStorage<String, Double> internalCache;
    protected RunnableFunc func;

    public interface RunnableFunc {
        double run(ArrayList<ArrayList<Integer>[]> biclusters);
    }

    public FitnessFunction(String[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache) {
        this.data = data;
        this.types = types;
        this.internalCache = internalCache;
    }

    public abstract double run(ArrayList<ArrayList<Integer>[]> biclusters);
    
    public interface ValueFunction {
        public float getFloatValue(String value, int col, ArrayList<Integer>[] bicluster);
    }

    protected static final Map<Class<?>, ValueFunction> conversionMap = new HashMap<>();

    static {
        conversionMap.put(Float.class, (value, col, bicluster) -> Float.parseFloat(value));
        conversionMap.put(Integer.class, (value, col, bicluster) -> Float.parseFloat(value));
        conversionMap.put(Double.class, (value, col, bicluster) -> Float.parseFloat(value));
        conversionMap.put(String.class, (value, col, bicluster) -> {
            // TODO: implement this
            return 0.0f;
        });
        conversionMap.put(Boolean.class, (value, col, bicluster) -> {
            // TODO: implement this
            return 0.0f;
        });
    }
}
