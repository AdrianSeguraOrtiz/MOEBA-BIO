package moeba.validation.metric.impl.scoreprelic.impl;

import java.util.ArrayList;

import moeba.validation.metric.impl.scoreprelic.ScorePrelic;

public class ScorePrelicRelevance extends ScorePrelic {

    public ScorePrelicRelevance(boolean saveProcess, String outputProcessFolder) {
        super(saveProcess, outputProcessFolder);
        if (saveProcess) System.out.println("This metric (ScorePrelicRelevance) doesnt support saving process, it will be ignored.");
    }

    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        return Math.sqrt(super.getScoreRows(inferredBiclusters, goldStandardBiclusters) * super.getScoreColumns(inferredBiclusters, goldStandardBiclusters));
    } 
    
    @Override
    public double getScoreSavingProcess(ArrayList<ArrayList<Integer>[]> inferredBiclusters,
            ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        return getScore(inferredBiclusters, goldStandardBiclusters, resultIndex);
    }
}
