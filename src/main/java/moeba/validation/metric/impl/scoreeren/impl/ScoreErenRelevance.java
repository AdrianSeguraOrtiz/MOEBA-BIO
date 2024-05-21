package moeba.validation.metric.impl.scoreeren.impl;

import java.io.PrintWriter;
import java.util.ArrayList;

import moeba.validation.metric.impl.scoreeren.ScoreEren;

public class ScoreErenRelevance extends ScoreEren {

    public ScoreErenRelevance(boolean saveProcess, String outputProcessFolder) {
        super(saveProcess, outputProcessFolder);
        this.outputProcessFolder += "/ScoreErenRelevance/";
    }

    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
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

    @Override
    public double getScoreSavingProcess(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        double sum = 0.0;
        PrintWriter writer = getScoreSavingProcessWriter(this.outputProcessFolder, resultIndex, goldStandardBiclusters.size());
        for (int i = 0; i < inferredBiclusters.size(); i++) {
            writer.print("Bicluster" + i + ",");
            double max = 0.0;
            for (int j = 0; j < goldStandardBiclusters.size(); j++) {
                double value = super.getJaccard(inferredBiclusters.get(i), goldStandardBiclusters.get(j));
                if (value > max) {
                    max = value;
                }
                writer.print(value);
                if (j < goldStandardBiclusters.size() - 1) writer.print(",");
            }
            sum += max;
            writer.println();
        }
        writer.close();
        
        return sum / inferredBiclusters.size();
    }
}
