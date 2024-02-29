package moeba.fitnessfunction;

import moeba.Representation;

public abstract class FitnessFunction {
    public interface FitnessFunctionInterface {
        public double run(Integer[] x);
    }

    private Object[][] data;
    private Class<?>[] types;
    private FitnessFunctionInterface fitnessFunctionInterface;

    public FitnessFunction(Object[][] data, Class<?>[] types, Representation representation) {
        this.data = data;
        this.types = types;
        if (representation == Representation.GENERIC) {
            this.fitnessFunctionInterface = (Integer[] x) -> {
                return runFromGenericRepresentation(x);
            };
        } else {
            this.fitnessFunctionInterface = (Integer[] x) -> {
                return runFromSpecificRepresentation(x);
            };
        }
    }

    public abstract double runFromGenericRepresentation(Integer[] x);
    public abstract double runFromSpecificRepresentation(Integer[] x);
    public double run(Integer[] x) {
        return fitnessFunctionInterface.run(x);
    }
}
