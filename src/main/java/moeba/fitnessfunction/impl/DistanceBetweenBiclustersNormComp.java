package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.GenericBiclusterFitnessFunction;
import moeba.utils.storage.CacheStorage;

public class DistanceBetweenBiclustersNormComp extends GenericBiclusterFitnessFunction {

    public DistanceBetweenBiclustersNormComp(double[][] data, Class<?>[] types,
            CacheStorage<String, Double> internalCache, String summariseIndividualObjectives) {
        super(data, types, internalCache, summariseIndividualObjectives);
    }

    @Override
    protected double getBiclusterScore(ArrayList<Integer>[] bicluster, ArrayList<ArrayList<Integer>[]> biclusters) {
        // Get the mean of the evaluated bicluster
        float sum = 0.0f;
        for (int i : bicluster[0]) {
            for (int j : bicluster[1]) {
                sum += data[i][j];
            }
        }
        float mean = sum / (bicluster[0].size() * bicluster[1].size());

        // Calculate the minimum variance to maximize it
        double res = Double.MAX_VALUE;
        for (ArrayList<Integer>[] b : biclusters) {
            double distance = 0.0;
            for (int i : b[0]) {
                for (int j : bicluster[1]) {
                    distance += Math.pow(data[i][j] - mean, 2);
                }
            }
            distance = distance / (b[0].size() * bicluster[1].size());

            if (distance < res) {
                res = distance;
            }
        }

        return res;
    }
    
}
