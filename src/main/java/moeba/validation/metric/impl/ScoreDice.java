package moeba.validation.metric.impl;

import java.io.PrintWriter;
import java.util.ArrayList;
import moeba.validation.metric.MetricInterface;

/**
 * Implementation of the Dice scoring metric for comparing biclusters.
 * This metric evaluates the similarity between sets of biclusters based on the Dice coefficient,
 * which is primarily used to gauge the similarity between two samples.
 * The Dice coefficient is calculated as twice the number of intersection elements divided by
 * the total number of elements in both sets.
 * This metric is based on the publication:
 * Lee R. Dice. 1945. Measures of the amount of ecologic association between species.
 * Ecology 26, 3 (1945), 297–302. DOI:https://doi.org/10.2307/1932409
 * 
 * This class extends MetricInterface and calculates the overall score based on this specific implementation.
 */
public class ScoreDice extends MetricInterface {

    public ScoreDice(boolean saveProcess, String outputProcessFolder) {
        super(saveProcess, outputProcessFolder);
        this.outputProcessFolder += "/ScoreDice/";
    }

    /**
     * Calculates the score by comparing each inferred bicluster with each gold standard bicluster,
     * determining the maximum Dice coefficient for each inferred bicluster against all gold standard biclusters.
     * The Dice coefficient for each pair of biclusters is calculated as twice the size of the intersection
     * of the Cartesian products of their row and column sets, divided by the sum of the sizes of these products.
     *
     * @param inferredBiclusters List of inferred biclusters, each represented by an array of two ArrayLists of integers.
     * @param goldStandardBiclusters List of gold standard biclusters, formatted similarly to inferred biclusters.
     * @param resultIndex Index of the result.
     * @return A double representing the overall score, computed as the average of the maximum Dice coefficients for each inferred bicluster.
     */
    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        double sum = 0.0;
        for (int i = 0; i < inferredBiclusters.size(); i++) {
            double max = 0.0;
            for (int j = 0; j < goldStandardBiclusters.size(); j++) {
                double value = 2.0 * (double) (getIntersectionOfPairs(getCartesianProduct(inferredBiclusters.get(i)[0], inferredBiclusters.get(i)[1]), getCartesianProduct(goldStandardBiclusters.get(j)[0], goldStandardBiclusters.get(j)[1])).length) / (getCartesianProduct(inferredBiclusters.get(i)[0], inferredBiclusters.get(i)[1]).length + getCartesianProduct(goldStandardBiclusters.get(j)[0], goldStandardBiclusters.get(j)[1]).length);
                if (value > max) {
                    max = value;
                }
            }
            sum += max;
        }
        return sum / inferredBiclusters.size();
    }

    /**
     * The same of the getScore method, but the results are saved in a file.
     *
     * @param inferredBiclusters List of inferred biclusters, each bicluster represented by an array of two ArrayLists of integers.
     * @param goldStandardBiclusters List of gold standard biclusters, formatted similarly to inferred biclusters.
     * @param resultIndex Index of the result.
     * @return A double representing the overall score, computed as the average of the maximum similarity scores for each inferred bicluster.
     */
    @Override
    public double getScoreSavingProcess(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        double sum = 0.0;
        PrintWriter writer = getScoreSavingProcessWriter(this.outputProcessFolder, resultIndex, goldStandardBiclusters.size());
        for (int i = 0; i < inferredBiclusters.size(); i++) {
            writer.print("Bicluster" + i + ",");
            double max = 0.0;
            for (int j = 0; j < goldStandardBiclusters.size(); j++) {
                double value = 2.0 * (double) (getIntersectionOfPairs(getCartesianProduct(inferredBiclusters.get(i)[0], inferredBiclusters.get(i)[1]), getCartesianProduct(goldStandardBiclusters.get(j)[0], goldStandardBiclusters.get(j)[1])).length) / (getCartesianProduct(inferredBiclusters.get(i)[0], inferredBiclusters.get(i)[1]).length + getCartesianProduct(goldStandardBiclusters.get(j)[0], goldStandardBiclusters.get(j)[1]).length);
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
