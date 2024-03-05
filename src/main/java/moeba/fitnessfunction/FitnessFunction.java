package moeba.fitnessfunction;

import java.util.ArrayList;

public abstract class FitnessFunction {
    protected Object[][] data;
    protected Class<?>[] types;

    public FitnessFunction(Object[][] data, Class<?>[] types) {
        this.data = data;
        this.types = types;
    }

    public abstract double run(ArrayList<ArrayList<Integer>[]> biclusters);
}
