package moeba.validation.metric.impl.scoreeren;

import java.util.ArrayList;
import moeba.validation.metric.MetricInterface;

/**
 * Abstract class representing a scoring mechanism based on the Jaccard index for comparing inferred biclusters
 * to a gold standard, following the methods described in:
 * 
 * K. Eren, M. Deveci, O. Kucuktunc, and U. V. Catalyurek. 2013. A comparative analysis of biclustering algorithms for
 * gene expression data. Brief. Bioinform. 14, 3 (2013), 279–292. DOI:https://doi.org/10.1093/bib/bbs032
 */
public abstract class ScoreEren extends MetricInterface {
    
    /**
     * Calculates the Jaccard similarity coefficient between two biclusters.
     * Each bicluster is represented as an array of two ArrayLists, where the first list contains row indices
     * and the second list contains column indices. The Jaccard coefficient is used to measure the similarity
     * between the two biclusters based on both their row and column overlaps.
     *
     * @param b1 First bicluster represented as an array of two ArrayLists of integers.
     * @param b2 Second bicluster similarly represented.
     * @return A double representing the Jaccard similarity coefficient, calculated as the ratio of the intersection
     *         of the Cartesian products of their row and column sets to the union of the same.
     */
    protected double getJaccard(ArrayList<Integer>[] b1, ArrayList<Integer>[] b2) {
        return (double) (getIntersectionOfPairs(getCartesianProduct(b1[0], b1[1]), getCartesianProduct(b2[0], b2[1])).length) / 
               (getUnionOfPairs(getCartesianProduct(b1[0], b1[1]), getCartesianProduct(b2[0], b2[1])).length);
    }
}
