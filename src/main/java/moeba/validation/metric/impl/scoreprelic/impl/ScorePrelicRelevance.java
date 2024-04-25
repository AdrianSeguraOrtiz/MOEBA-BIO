package moeba.validation.metric.impl.scoreprelic.impl;

import java.util.ArrayList;

import moeba.validation.metric.impl.scoreprelic.ScorePrelic;

public class ScorePrelicRelevance extends ScorePrelic {

    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters) {
        return Math.sqrt(super.getScoreRows(inferredBiclusters, goldStandardBiclusters) * super.getScoreColumns(inferredBiclusters, goldStandardBiclusters));
    } 
    
}
