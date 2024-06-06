package moeba.fitnessfunction;

import java.util.ArrayList;

public abstract class FitnessFunction {
    protected double[][] data;
    protected Class<?>[] types;
    protected RunnableFunc func;

    public interface RunnableFunc {
        double run(ArrayList<ArrayList<Integer>[]> biclusters);
    }

    public FitnessFunction(double[][] data, Class<?>[] types) {
        this.data = data;
        this.types = types;
    }

    public abstract double run(ArrayList<ArrayList<Integer>[]> biclusters);
    
}
