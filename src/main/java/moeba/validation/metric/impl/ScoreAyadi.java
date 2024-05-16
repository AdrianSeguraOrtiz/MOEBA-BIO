package moeba.validation.metric.impl;

import java.io.PrintWriter;
import java.util.ArrayList;
import moeba.validation.metric.MetricInterface;

/**
 * Implementation of the Ayadi scoring metric for comparing biclusters.
 * This metric evaluates the similarity between sets of biclusters based on a product
 * of intersections over a product of unions for rows and columns separately,
 * which is a method detailed in the publication:
 * Wassim Ayadi, Ons Maatouk, and Hend Bouziri. 2012. Evolutionary biclustering algorithm of gene expression data.
 * In International Workshop on Database and Expert Systems Applications. IEEE, 206–210. DOI:https://doi.org/10.1109/dexa.2012.46
 * 
 * This class extends MetricInterface and calculates the overall score based on this specific implementation.
 */
public class ScoreAyadi extends MetricInterface {

    public ScoreAyadi(boolean saveProcess, String outputProcessFolder) {
        super(saveProcess, outputProcessFolder);
        this.outputProcessFolder += "/scoreAyadi/";
    }

    /**
     * Calculates the score by comparing each inferred bicluster with each gold standard bicluster,
     * determining the maximum similarity score for each inferred bicluster against all gold standard biclusters.
     * The similarity score for each pair of biclusters is calculated as the product of the intersections
     * of their row and column sets divided by the product of the unions of their row and column sets.
     *
     * @param inferredBiclusters List of inferred biclusters, each represented by an array of two ArrayLists of integers.
     * @param goldStandardBiclusters List of gold standard biclusters, formatted similarly to inferred biclusters.
     * @param resultIndex Index of the result.
     * @return A double representing the overall score, computed as the average of the maximum similarity scores for each inferred bicluster.
     */
    @Override
    public double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        double sum = 0.0;
        for (int i = 0; i < inferredBiclusters.size(); i++) {
            double max = 0.0;
            for (int j = 0; j < goldStandardBiclusters.size(); j++) {
                double value = (double) (getIntersection(inferredBiclusters.get(i)[0], goldStandardBiclusters.get(j)[0]).length * getIntersection(inferredBiclusters.get(i)[1], goldStandardBiclusters.get(j)[1]).length) / (getUnion(inferredBiclusters.get(i)[0], goldStandardBiclusters.get(j)[0]).length * getUnion(inferredBiclusters.get(i)[1], goldStandardBiclusters.get(j)[1]).length);
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
                double value = (double) (getIntersection(inferredBiclusters.get(i)[0], goldStandardBiclusters.get(j)[0]).length * getIntersection(inferredBiclusters.get(i)[1], goldStandardBiclusters.get(j)[1]).length) / (getUnion(inferredBiclusters.get(i)[0], goldStandardBiclusters.get(j)[0]).length * getUnion(inferredBiclusters.get(i)[1], goldStandardBiclusters.get(j)[1]).length);
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
