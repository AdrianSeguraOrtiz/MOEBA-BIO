package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.FitnessFunction;

public class BiclusterSize extends FitnessFunction {
    public BiclusterSize(Object[][] data, Class<?>[] types) {
        super(data, types);
    }

    @Override
    public double run(ArrayList<ArrayList<Integer>[]> biclusters) {
        int res = 0;
        for (ArrayList<Integer>[] bic : biclusters) {
            res += bic[0].size() * bic[1].size();
        }
        return 1 - ((double) res)/(data.length * data[0].length);
    }
    
}
