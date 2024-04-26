package moeba.validation.metric.impl;

import java.util.ArrayList;
import moeba.validation.metric.MetricInterface;

/**
 * Implementation of the Liu and Wang scoring metric for comparing biclusters.
 * This metric evaluates the similarity between sets of biclusters based on the proportion
 * of intersection over union for both rows and columns, similar to the Jaccard coefficient.
 * The metric is based on the publication:
 * X. Liu and L. Wang. 2007. Computing the maximum similarity bi-clusters of gene expression data.
 * Bioinformatics 23, 1 (2007), 50–56. DOI:https://doi.org/10.1093/bioinformatics/btl560
 * 
 * This class extends MetricInterface and calculates the overall score based on this specific implementation.
 */
public class ScoreLiuWang extends MetricInterface {

    /**
     * Calculates the score by comparing each inferred bicluster with each gold standard bicluster,
     * and then determines the maximum similarity score for each inferred bicluster against all gold standard biclusters.
     * The score for each pair of biclusters is computed as the sum of the lengths of intersections
     * divided by the sum of the lengths of unions for both row and column sets.
     *
     * @param inferredBiclusters List of inferred biclusters, each bicluster represented by an array of two ArrayLists of integers.
     * @param goldStandardBiclusters List of gold standard biclusters, formatted similarly to inferred biclusters.
     * @return A double representing the overall score, computed as the average of the maximum similarity scores for each inferred bicluster.
     */
    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters) {
        double sum = 0.0;
        for (int i = 0; i < inferredBiclusters.size(); i++) {
            double max = 0.0;
            for (int j = 0; j < goldStandardBiclusters.size(); j++) {
                double value = (double) (getIntersection(inferredBiclusters.get(i)[0], goldStandardBiclusters.get(j)[0]).length + getIntersection(inferredBiclusters.get(i)[1], goldStandardBiclusters.get(j)[1]).length) / (getUnion(inferredBiclusters.get(i)[0], goldStandardBiclusters.get(j)[0]).length + getUnion(inferredBiclusters.get(i)[1], goldStandardBiclusters.get(j)[1]).length);
                if (value > max) {
                    max = value;
                }
            }
            sum += max;
        }
        return sum / inferredBiclusters.size();
    }
}
