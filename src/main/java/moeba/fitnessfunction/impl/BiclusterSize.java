package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.IndividualFitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterSize extends IndividualFitnessFunction {

    public BiclusterSize(String[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache, String summariseIndividualObjectives) {
        super(data, types, internalCache, summariseIndividualObjectives);
    }

    protected double getBiclusterScore(ArrayList<Integer>[] bicluster) {
        return (double) (bicluster[0].size() * bicluster[1].size()) / (data.length * data[0].length);
    }
}
