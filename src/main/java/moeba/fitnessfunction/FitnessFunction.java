package moeba.fitnessfunction;

import java.util.ArrayList;

public abstract class FitnessFunction {
    private Object[][] data;
    private Class<?>[] types;

    public FitnessFunction(Object[][] data, Class<?>[] types) {
        this.data = data;
        this.types = types;
    }

    public abstract double run(ArrayList<ArrayList<Integer>[]> biclusters);
}
