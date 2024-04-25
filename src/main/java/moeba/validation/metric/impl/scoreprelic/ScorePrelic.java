package moeba.validation.metric.impl.scoreprelic;

import java.util.ArrayList;

import moeba.validation.metric.MetricInterface;

/**
 * Abstract class representing a scoring mechanism for comparing inferred biclusters
 * to a gold standard. This class implements methods to calculate scores based on the
 * overlap of rows and columns between biclusters from two different sources.
 * 
 * Ref: Amela Prelić, Stefan Bleuler, Philip Zimmermann, Anja Wille, Peter Bühlmann, Wilhelm Gruissem, Lars Hennig,
 * Lothar Thiele, and Eckart Zitzler. 2006. A systematic comparison and evaluation of biclustering methods for gene
 * expression data. Bioinformatics 22, 9 (2006), 1122–1129. DOI:https://doi.org/10.1093/bioinformatics/btl060
 */
public abstract class ScorePrelic extends MetricInterface {
    
    public abstract double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters);

    /**
     * Calculate the score based on the row overlaps between two lists of biclusters.
     *
     * @param bics1 First list of biclusters, where each bicluster is represented as an array (first array for rows).
     * @param bics2 Second list of biclusters, compared against the first.
     * @return A double representing the average maximum similarity score based on rows.
     */
    protected double getScoreRows(ArrayList<ArrayList<Integer>[]> bics1, ArrayList<ArrayList<Integer>[]> bics2) {
        double sum = 0.0;
        for (int i = 0; i < bics1.size(); i++) {
            double max = 0.0;
            for (int j = 0; j < bics2.size(); j++) {
                double value = (double) getIntersection(bics1.get(i)[0], bics2.get(j)[0]).length / getUnion(bics1.get(i)[0], bics2.get(j)[0]).length;
                if (value > max) {
                    max = value;
                }
            }
            sum += max;
        }
        return sum / bics1.size();
    }

    /**
     * Calculate the score based on the column overlaps between two lists of biclusters.
     *
     * @param bics1 First list of biclusters, where each bicluster is represented as an array (second array for columns).
     * @param bics2 Second list of biclusters, compared against the first.
     * @return A double representing the average maximum similarity score based on columns.
     */
    protected double getScoreColumns(ArrayList<ArrayList<Integer>[]> bics1, ArrayList<ArrayList<Integer>[]> bics2) {
        double sum = 0.0;
        for (int i = 0; i < bics1.size(); i++) {
            double max = 0.0;
            for (int j = 0; j < bics2.size(); j++) {
                double value = (double) getIntersection(bics1.get(i)[1], bics2.get(j)[1]).length / getUnion(bics1.get(i)[1], bics2.get(j)[1]).length;
                if (value > max) {
                    max = value;
                }
            }
            sum += max;
        }
        return sum / bics1.size();
    }
}
