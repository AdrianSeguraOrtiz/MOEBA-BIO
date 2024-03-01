package moeba.fitnessfunction;

public abstract class FitnessFunction {
    private Object[][] data;
    private Class<?>[] types;

    public FitnessFunction(Object[][] data, Class<?>[] types) {
        this.data = data;
        this.types = types;
    }

    public abstract double run(Integer[][][] biclusters);
}
