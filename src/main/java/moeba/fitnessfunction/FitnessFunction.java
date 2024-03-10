package moeba.fitnessfunction;

import java.util.ArrayList;
import java.util.Map;

public abstract class FitnessFunction {
    protected Object[][] data;
    protected Class<?>[] types;
    protected Map<String, Double> cache;
    private RunnableFunc func;

    public interface RunnableFunc {
        double run(ArrayList<ArrayList<Integer>[]> biclusters);
    }

    public FitnessFunction(Object[][] data, Class<?>[] types, Map<String, Double> cache) {
        this.data = data;
        this.types = types;
        this.cache = cache;
        this.func = cache == null ? this::runWithoutCache : this::runWithCache;
    }

    protected abstract double runWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters);
    protected abstract double runWithCache(ArrayList<ArrayList<Integer>[]> biclusters);

    public double run(ArrayList<ArrayList<Integer>[]> biclusters) {
        return func.run(biclusters);
    }
}
