package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.StaticUtils;
import moeba.fitnessfunction.FitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterSizeWeighted extends FitnessFunction {

    public BiclusterSizeWeighted(Object[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache) {
        super(data, types, internalCache);
    }

    @Override
    protected double runWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        for (ArrayList<Integer>[] bic : biclusters) {
            res += getBiclusterScore(bic);
        }
        return 1 - res/biclusters.size();
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
        return 1 - res/biclusters.size();
    }

    protected double getBiclusterScore(ArrayList<Integer>[] bicluster) {
        return (double) (bicluster[0].size()*0.75 / data.length + bicluster[1].size()*0.25/data[0].length);
    }
}