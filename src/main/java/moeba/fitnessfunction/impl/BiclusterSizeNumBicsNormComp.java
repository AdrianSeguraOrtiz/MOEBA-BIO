package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.GenericBiclusterFitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterSizeNumBicsNormComp extends GenericBiclusterFitnessFunction {
    private int growthRate;
    private double initialWeight;

    public BiclusterSizeNumBicsNormComp(double[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache,
            String summariseIndividualObjectives, int growthRate, double initialWeight) {
        super(data, types, internalCache, summariseIndividualObjectives);
        this.growthRate = growthRate;
        this.initialWeight = initialWeight;
    }

    @Override
    protected double getBiclusterScore(ArrayList<Integer>[] bicluster, ArrayList<ArrayList<Integer>[]> biclusters) {
        int maxSize = data.length * data[0].length;
        int numBics = biclusters.size() + 1;
        double parcelSize = (double) maxSize / Math.pow(numBics, 2);
        int biclusterSize = bicluster[0].size() * bicluster[1].size();

        return (initialWeight + (1 - initialWeight) * (1 - Math.pow(1 - ((parcelSize / maxSize) * numBics), growthRate))) * (1 - Math.min(1, Math.abs(parcelSize - biclusterSize) / parcelSize));
    }
    
}
