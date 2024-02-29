package moeba.fitnessfunction.impl;

import moeba.Representation;
import moeba.fitnessfunction.FitnessFunction;

public class BiclusterSize extends FitnessFunction {
    public BiclusterSize(Object[][] data, Class<?>[] types, Representation representation) {
        super(data, types, representation);
    }

    @Override
    public double runFromGenericRepresentation(Integer[] x) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'runFromGenericRepresentation'");
    }

    @Override
    public double runFromSpecificRepresentation(Integer[] x) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'runFromSpecificRepresentation'");
    }
    
}
