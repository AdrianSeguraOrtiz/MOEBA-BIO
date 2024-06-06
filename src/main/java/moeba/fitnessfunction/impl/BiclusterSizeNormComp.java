package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.IndividualBiclusterFitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterSizeNormComp extends IndividualBiclusterFitnessFunction {

    public BiclusterSizeNormComp(double[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache, String summariseIndividualObjectives) {
        super(data, types, internalCache, summariseIndividualObjectives);
    }

    protected double getBiclusterScore(ArrayList<Integer>[] bicluster) {
        return (double) (bicluster[0].size() * bicluster[1].size()) / (data.length * data[0].length);
    }
}
