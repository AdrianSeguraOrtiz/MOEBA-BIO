package moeba.fitnessfunction.impl;

import moeba.fitnessfunction.FitnessFunction;

public class BiclusterSize extends FitnessFunction {
    public BiclusterSize(Object[][] data, Class<?>[] types) {
        super(data, types);
    }

    @Override
    public double run(Integer[][][] biclusters) {
        return 1.0;
    }
    
}
