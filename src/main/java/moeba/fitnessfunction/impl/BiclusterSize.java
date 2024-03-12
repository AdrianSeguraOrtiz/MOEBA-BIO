package moeba.fitnessfunction.impl;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import moeba.StaticUtils;
import moeba.fitnessfunction.FitnessFunction;

public class BiclusterSize extends FitnessFunction {

    public BiclusterSize(Object[][] data, Class<?>[] types, ConcurrentHashMap<String, Double> internalCache) {
        super(data, types, internalCache);
    }

    @Override
    protected double runWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        for (ArrayList<Integer>[] bic : biclusters) {
            res += getBiclusterScore(bic);
        }
        return 1 - res/(data.length * data[0].length);
    }

    @Override
    protected double runWithCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        String key;
        double bicScore;
        for (ArrayList<Integer>[] bic : biclusters) {
            key = StaticUtils.biclusterToString(bic);
            if (internalCache.containsKey(key)){
                bicScore = internalCache.get(key);
            } else {
                bicScore = getBiclusterScore(bic);
                internalCache.put(key, bicScore);
            }
            res += bicScore;
        }
        return 1 - res/(data.length * data[0].length);
    }

    protected double getBiclusterScore(ArrayList<Integer>[] bicluster) {
        return (double) (bicluster[0].size() * bicluster[1].size());
    }
}
