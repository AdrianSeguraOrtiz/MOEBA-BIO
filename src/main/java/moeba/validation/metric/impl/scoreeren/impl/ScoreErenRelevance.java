package moeba.validation.metric.impl.scoreeren.impl;

import java.util.ArrayList;

import moeba.validation.metric.impl.scoreeren.ScoreEren;

public class ScoreErenRelevance extends ScoreEren {

    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters) {
        double sum = 0.0;
        for (int i = 0; i < inferredBiclusters.size(); i++) {
            double max = 0.0;
            for (int j = 0; j < goldStandardBiclusters.size(); j++) {
                double value = super.getJaccard(inferredBiclusters.get(i), goldStandardBiclusters.get(j));
                if (value > max) {
                    max = value;
                }
            }
            sum += max;
        }
        return sum / inferredBiclusters.size();
    }
    
}
