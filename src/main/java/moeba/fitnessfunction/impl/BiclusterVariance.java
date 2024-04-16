package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.FitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterVariance extends FitnessFunction {

    public BiclusterVariance(String[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache) {
        super(data, types, internalCache);
    }

    @Override
    public double run(ArrayList<ArrayList<Integer>[]> biclusters) {
        return super.func.run(biclusters);
    }

    @Override
    protected double getBiclusterScore(ArrayList<Integer>[] bicluster) {
        // TODO: Refactor to not repeat conversion
        
        // Get the mean of the bicluster
        float sum = 0.0f;
        for (int i : bicluster[0]) {
            for (int j : bicluster[1]) {
                sum += conversionMap.get(types[j]).getFloatValue(data[i][j], j, bicluster);
            }
        }
        float mean = sum / (bicluster[0].size() * bicluster[1].size());

        // Sum the squared differences of the bicluster values from the mean
        double score = 0.0;
        for (int i : bicluster[0]) {
            for (int j : bicluster[1]) {
                score += Math.pow(conversionMap.get(types[j]).getFloatValue(data[i][j], j, bicluster) - mean, 2);
            }
        }

        return score;
    }
}
