package moeba.validation.metric.impl.scoreeren.impl;

import java.io.PrintWriter;
import java.util.ArrayList;

import moeba.validation.metric.impl.scoreeren.ScoreEren;

public class ScoreErenRecovery extends ScoreEren {

    public ScoreErenRecovery(boolean saveProcess, String outputProcessFolder) {
        super(saveProcess, outputProcessFolder);
        this.outputProcessFolder += "/ScoreErenRecovery/";
    }

    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        double sum = 0.0;
        for (int i = 0; i < goldStandardBiclusters.size(); i++) {
            double max = 0.0;
            for (int j = 0; j < inferredBiclusters.size(); j++) {
                double value = super.getJaccard(goldStandardBiclusters.get(i), inferredBiclusters.get(j));
                if (value > max) {
                    max = value;
                }
            }
            sum += max;
        }
        return sum / goldStandardBiclusters.size();
    }
    
    @Override
    public double getScoreSavingProcess(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        double sum = 0.0;
        PrintWriter writer = getScoreSavingProcessWriter(this.outputProcessFolder, resultIndex, goldStandardBiclusters.size());
        double[][] scores = new double[inferredBiclusters.size()][goldStandardBiclusters.size()];
        for (int i = 0; i < goldStandardBiclusters.size(); i++) {
            double max = 0.0;
            for (int j = 0; j < inferredBiclusters.size(); j++) {
                double value = super.getJaccard(goldStandardBiclusters.get(i), inferredBiclusters.get(j));
                if (value > max) {
                    max = value;
                }
                scores[j][i] = value;
            }
            sum += max;
        }

        for (int i = 0; i < inferredBiclusters.size(); i++) {
            writer.print("Bicluster" + i + ",");
            for (int j = 0; j < goldStandardBiclusters.size(); j++) {
                writer.print(scores[i][j]);
                if (j < goldStandardBiclusters.size() - 1) writer.print(",");
            }
            writer.println();
        }
        writer.close();
        
        return sum / goldStandardBiclusters.size();
    }
    
}