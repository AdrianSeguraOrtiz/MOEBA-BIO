package moeba.fitnessfunction.impl;

import moeba.Representation;
import moeba.fitnessfunction.FitnessFunction;

public class BiclusterSize extends FitnessFunction {
    public BiclusterSize(Object[][] data, Class<?>[] types, Representation representation) {
        super(data, types, representation);
    }

    @Override
    public double runFromGenericRepresentation(Integer[] x) {
        return 1.0;
    }

    @Override
    public double runFromSpecificRepresentation(Integer[] x) {
        return 1.0;
    }
    
}
