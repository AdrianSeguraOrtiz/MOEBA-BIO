package moeba.validation.metric.impl.scoreprelic.impl;

import java.util.ArrayList;

import moeba.validation.metric.impl.scoreprelic.ScorePrelic;

public class ScorePrelicRecovery extends ScorePrelic {

    public ScorePrelicRecovery(boolean saveProcess, String outputProcessFolder) {
        super(saveProcess, outputProcessFolder);
        if (saveProcess) System.out.println("This metric (ScorePrelicRecovery) doesnt support saving process, it will be ignored.");
    }

    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        return Math.sqrt(super.getScoreRows(goldStandardBiclusters, inferredBiclusters) * super.getScoreColumns(goldStandardBiclusters, inferredBiclusters));
    }
    
    @Override
    public double getScoreSavingProcess(ArrayList<ArrayList<Integer>[]> inferredBiclusters,
            ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        return getScore(inferredBiclusters, goldStandardBiclusters, resultIndex);
    }
}
