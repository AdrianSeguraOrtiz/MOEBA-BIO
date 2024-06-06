package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.IndividualBiclusterFitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterSizeWeightedNormComp extends IndividualBiclusterFitnessFunction {

    public BiclusterSizeWeightedNormComp(double[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache, String summariseIndividualObjectives) {
        super(data, types, internalCache, summariseIndividualObjectives);
    }

    protected double getBiclusterScore(ArrayList<Integer>[] bicluster) {
        return (double) (bicluster[0].size()*0.75 / data.length + bicluster[1].size()*0.25 / data[0].length);
    }
}