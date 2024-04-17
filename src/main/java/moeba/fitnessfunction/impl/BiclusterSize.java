package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.FitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterSize extends FitnessFunction {

    public BiclusterSize(String[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache) {
        super(data, types, internalCache);
    }

    @Override
    public double run(ArrayList<ArrayList<Integer>[]> biclusters) {
        return 1 - super.func.run(biclusters);
    }

    protected double getBiclusterScore(ArrayList<Integer>[] bicluster) {
        return (double) (bicluster[0].size() * bicluster[1].size()) / (data.length * data[0].length);
    }
}
