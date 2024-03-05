package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.FitnessFunction;

public class BiclusterSize extends FitnessFunction {
    public BiclusterSize(Object[][] data, Class<?>[] types) {
        super(data, types);
    }

    @Override
    public double run(ArrayList<ArrayList<Integer>[]> biclusters) {
        return 1.0;
    }
    
}
