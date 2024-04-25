package moeba.validation.metric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public abstract class MetricInterface {

    /**
     * Calculate the score by comparing two sets of biclusters, one inferred and one as gold standard.
     *
     * @param inferredBiclusters List of biclusters inferred from some analysis method.
     * @param goldStandardBiclusters List of biclusters considered as the gold standard for comparison.
     * @return A double representing the overall score based on specific implementation.
     */
    public abstract double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters);    

    /**
     * Calculates the intersection of two ArrayLists of integers.
     * Intersection means elements that appear in both lists without duplicates.
     *
     * @param a the first ArrayList of integers
     * @param b the second ArrayList of integers
     * @return an array of integers representing the intersection
     */
    public int[] getIntersection(ArrayList<Integer> a, ArrayList<Integer> b) {
        Set<Integer> setA = new HashSet<>();
        Set<Integer> intersection = new HashSet<>();

        // Add all elements from ArrayList a to setA
        for (Integer num : a) {
            setA.add(num);
        }

        // Check each element from ArrayList b; if it exists in setA, add to intersection
        for (Integer num : b) {
            if (setA.contains(num)) {
                intersection.add(num);
            }
        }

        // Convert the intersection set to an array
        int[] result = new int[intersection.size()];
        int i = 0;
        for (Integer num : intersection) {
            result[i++] = num;
        }

        return result;
    }

    /**
     * Calculates the union of two ArrayLists of integers.
     * Union means all elements from both lists, but no duplicates.
     *
     * @param a the first ArrayList of integers
     * @param b the second ArrayList of integers
     * @return an array of integers representing the union
     */
    public int[] getUnion(ArrayList<Integer> a, ArrayList<Integer> b) {
        Set<Integer> setUnion = new HashSet<>();

        // Add all elements from both ArrayLists a and b to setUnion
        for (Integer num : a) {
            setUnion.add(num);
        }
        for (Integer num : b) {
            setUnion.add(num);
        }

        // Convert the union set to an array
        int[] result = new int[setUnion.size()];
        int i = 0;
        for (Integer num : setUnion) {
            result[i++] = num;
        }

        return result;
    }

}
