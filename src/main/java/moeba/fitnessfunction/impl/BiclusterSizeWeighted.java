package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.FitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterSizeWeighted extends FitnessFunction {

    public BiclusterSizeWeighted(String[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache) {
        super(data, types, internalCache);
    }

    @Override
    public double run(ArrayList<ArrayList<Integer>[]> biclusters) {
        return 1 - super.func.run(biclusters);
    }

    protected double getBiclusterScore(ArrayList<Integer>[] bicluster) {
        return (double) (bicluster[0].size()*0.75 / data.length + bicluster[1].size()*0.25 / data[0].length);
    }
}