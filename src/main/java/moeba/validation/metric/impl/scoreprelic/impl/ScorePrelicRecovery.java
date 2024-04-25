package moeba.validation.metric.impl.scoreprelic.impl;

import java.util.ArrayList;

import moeba.validation.metric.impl.scoreprelic.ScorePrelic;

public class ScorePrelicRecovery extends ScorePrelic {

    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters) {
        return Math.sqrt(super.getScoreRows(goldStandardBiclusters, inferredBiclusters) * super.getScoreColumns(goldStandardBiclusters, inferredBiclusters));
    }
    
}
