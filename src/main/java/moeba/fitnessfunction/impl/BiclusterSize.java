package moeba.fitnessfunction.impl;

import java.util.ArrayList;
import java.util.Map;

import moeba.fitnessfunction.FitnessFunction;

public class BiclusterSize extends FitnessFunction {

    public BiclusterSize(Object[][] data, Class<?>[] types, Map<String, Double> cache) {
        super(data, types, cache);
    }

    @Override
    public double runWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        int res = 0;
        for (ArrayList<Integer>[] bic : biclusters) {
            res += bic[0].size() * bic[1].size();
        }
        return 1 - ((double) res)/(data.length * data[0].length);
    }

    @Override
    protected double runWithCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'runWithCache'");
    }
    
}
