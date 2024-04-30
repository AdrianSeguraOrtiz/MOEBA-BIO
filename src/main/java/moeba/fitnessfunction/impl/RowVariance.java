package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.IndividualFitnessFunction;
import moeba.utils.storage.CacheStorage;

public class RowVariance extends IndividualFitnessFunction {

    public RowVariance(String[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache, String summariseIndividualObjectives) {
        super(data, types, internalCache, summariseIndividualObjectives);
    }

    @Override
    protected double getBiclusterScore(ArrayList<Integer>[] bicluster) {  
        // Get the mean of each row in the bicluster
        float[] means = new float[bicluster[0].size()];
        int cnt = 0;
        for (int i : bicluster[0]) {
            float sum = 0.0f;
            for (int j : bicluster[1]) {
                sum += conversionMap.get(types[j]).getFloatValue(data[i][j], j, bicluster);
            }
            means[cnt++] = sum / bicluster[1].size();
        }

        // Sum the squared differences of the bicluster values from the mean for each row
        double score = 0.0;
        cnt = 0;
        for (int i : bicluster[0]) {
            for (int j : bicluster[1]) {
                score += Math.pow(conversionMap.get(types[j]).getFloatValue(data[i][j], j, bicluster) - means[cnt], 2);
            }
            cnt++;
        }

        // Divide by the number of elements in the bicluster
        score /= (bicluster[0].size() * bicluster[1].size());

        // Revert to maximization and normalize between 0 and 1
        return 1 - 4 * score;
    }
}
