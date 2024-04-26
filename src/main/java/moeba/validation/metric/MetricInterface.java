package moeba.validation.metric;

import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Calculates the Cartesian product of two ArrayLists of integers.
     * Cartesian product means all ordered pairs (a, b) where 'a' is from the first list
     * and 'b' is from the second list.
     *
     * @param a the first ArrayList of integers
     * @param b the second ArrayList of integers
     * @return an int[][] where each inner int[] represents an ordered pair (a, b)
     */
    public int[][] getCartesianProduct(ArrayList<Integer> a, ArrayList<Integer> b) {
        int[][] cartesianProduct = new int[a.size() * b.size()][];

        int index = 0;
        // Iterate over each element in the first ArrayList
        for (Integer elementA : a) {
            // For each element in the first list, iterate over each element in the second list
            for (Integer elementB : b) {
                // Create a new pair (elementA, elementB) and add it to the array
                cartesianProduct[index++] = new int[]{elementA, elementB};
            }
        }

        return cartesianProduct;
    }

    /**
     * Calculates the union of two sets of pairs (as int[][]).
     * Union means all unique pairs from both arrays.
     *
     * @param first the first int[][] of pairs
     * @param second the second int[][] of pairs
     * @return an int[][] representing the union of the pairs
     */
    public int[][] getUnionOfPairs(int[][] first, int[][] second) {
        Set<String> set = new HashSet<>();
        
        // Add all pairs from the first array
        for (int[] pair : first) {
            set.add(Arrays.toString(pair));
        }
        
        // Add all pairs from the second array
        for (int[] pair : second) {
            set.add(Arrays.toString(pair));
        }
        
        int[][] result = new int[set.size()][2];
        int index = 0;
        
        // Convert the set back to an array of int arrays
        for (String pairString : set) {
            result[index++] = Arrays.stream(pairString.substring(1, pairString.length() - 1).split(", "))
                                    .mapToInt(Integer::parseInt)
                                    .toArray();
        }
        
        return result;
    }

    /**
     * Calculates the intersection of two sets of pairs (as int[][]).
     * Intersection means pairs that appear in both arrays.
     *
     * @param first the first int[][] of pairs
     * @param second the second int[][] of pairs
     * @return an int[][] representing the intersection of the pairs
     */
    public int[][] getIntersectionOfPairs(int[][] first, int[][] second) {
        Set<String> setFirst = new HashSet<>();
        Set<String> setSecond = new HashSet<>();
        
        for (int[] pair : first) {
            setFirst.add(Arrays.toString(pair));
        }
        
        for (int[] pair : second) {
            setSecond.add(Arrays.toString(pair));
        }
        
        // Keep only the elements that are present in both sets
        setFirst.retainAll(setSecond);
        
        int[][] result = new int[setFirst.size()][2];
        int index = 0;
        
        // Convert the set back to an array of int arrays
        for (String pairString : setFirst) {
            result[index++] = Arrays.stream(pairString.substring(1, pairString.length() - 1).split(", "))
                                    .mapToInt(Integer::parseInt)
                                    .toArray();
        }
        
        return result;
    }


}
