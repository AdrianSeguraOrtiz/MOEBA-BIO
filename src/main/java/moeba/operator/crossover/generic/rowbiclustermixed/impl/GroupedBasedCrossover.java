package moeba.operator.crossover.generic.rowbiclustermixed.impl;

import org.uma.jmetal.util.binarySet.BinarySet;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import moeba.operator.crossover.generic.rowbiclustermixed.RowBiclusterMixedCrossover;

import java.util.ArrayList;

import org.uma.jmetal.solution.integersolution.IntegerSolution;

/**
 * Implements a grouped-based crossover strategy specific to the Row Bicluster Mixed Crossover interface.
 * This class is designed to mix genetic information between two IntegerSolution parents, using a dynamic and adaptable crossover strategy.
 */
public class GroupedBasedCrossover implements RowBiclusterMixedCrossover {
    private Random random; // Used for generating random numbers throughout the crossover process
    private AtomicInteger numOperations; // Atomic counter to keep track of the number of crossover operations performed
    private int numApproxCrossovers; // Total number of approximate crossovers to perform
    private float shuffleEnd; // Determines the point in the crossover process where shuffling ends
    private float dynamicStartAmount; // Used to dynamically adjust the amount of biclusters to be crossed

    /**
     * Constructor for the GroupedBasedCrossover class without a predefined random object.
     * Initializes the class with specified parameters for the crossover process.
     * 
     * @param numApproxCrossovers The total number of approximate crossovers to perform.
     * @param shuffleEnd The point in the crossover process where shuffling ends.
     * @param dynamicStartAmount Used to dynamically adjust the amount of biclusters to be crossed.
     */
    public GroupedBasedCrossover(int numApproxCrossovers, float shuffleEnd, float dynamicStartAmount) {
        this.numApproxCrossovers = numApproxCrossovers;
        this.shuffleEnd = shuffleEnd;
        this.dynamicStartAmount = dynamicStartAmount;
        this.random = new Random();
        this.numOperations = new AtomicInteger();
    }

    /**
     * Constructor for the GroupedBasedCrossover class with a predefined random object.
     * This allows for a controlled randomization, useful in testing or specific scenarios.
     * 
     * @param numApproxCrossovers The total number of approximate crossovers to perform.
     * @param shuffleEnd The point in the crossover process where shuffling ends.
     * @param dynamicStartAmount Used to dynamically adjust the amount of biclusters to be crossed.
     * @param random The Random object to use for generating random numbers.
     */
    public GroupedBasedCrossover(int numApproxCrossovers, float shuffleEnd, float dynamicStartAmount, Random random) {
        this.numApproxCrossovers = numApproxCrossovers;
        this.shuffleEnd = shuffleEnd;
        this.dynamicStartAmount = dynamicStartAmount;
        this.random = random;
        this.numOperations = new AtomicInteger();
    }

    /**
     * Executes the crossover operation between two parents.
     * This method uses dynamic parameters to adjust the crossover process based on the progress of the operation,
     * aiming to dynamically control the amount of genetic information exchanged.
     * 
     * @param is1 The first parent (IntegerSolution) involved in the crossover.
     * @param is2 The second parent (IntegerSolution) involved in the crossover.
     * @param bs1 A BinarySet representing the genetic information of the first parent that will be crossed.
     * @param bs2 A BinarySet representing the genetic information of the second parent that will be crossed.
     */
    @Override
    public void execute(IntegerSolution is1, IntegerSolution is2, BinarySet bs1, BinarySet bs2) {
        
        // Calculate the percentage of crossover operations completed to adjust the dynamic parameters
        float doned = (float) numOperations.getAndIncrement() / this.numApproxCrossovers;

        // Determine the amount of biclusters to cross based on the dynamic start amount and the progress
        float amount = (1 - dynamicStartAmount) * doned + dynamicStartAmount;

        // Ensure the last bit of each individual is set to true to match their cardinality with the number of biclusters
        int n = is1.variables().size();

        // Determine the number of biclusters to cross for each individual, ensuring at least one is crossed
        int numBicsP1 = Math.max((int) ((bs1.cardinality() + (bs1.get(n -1) ? 0 : 1 )) * amount), 1);
        int numBicsP2 = Math.max((int) ((bs2.cardinality() + (bs2.get(n -1) ? 0 : 1 )) * amount), 1);

        // Calculate the range of positions for the biclusters to be crossed
        int[] limits1 = amount != 1 ? getLimits(bs1, random.nextInt(n-2)+1, numBicsP1, n) : new int[] {-1, n-1};
        int[] limits2 = amount != 1 ? getLimits(bs2, random.nextInt(n-2)+1, numBicsP2, n) : new int[] {-1, n-1};
        if (limits1[0] == 0 && !bs1.get(0)) limits1[0] = -1;
        if (limits2[0] == 0 && !bs2.get(0)) limits2[0] = -1;
        
        // The bics vector stores at position i the identifier of the bicluster to which row i belongs. If row i has been out of range, it will be assigned the value 0
        // The cuts vector stores at position i the cut point of bicluster i. Since bicluster 0 represents out-of-range leftovers, cuts[0] will be left at the default value of 0.
        // For individual 1:
        int b1 = 1;
        int size1, maxSize1 = 0, minSize1 = n;
        int[] bicsP1 = new int[n];
        int[] cutsP1 = new int[numBicsP1 + 1];
        for (int i = limits1[0]+1; i < limits1[1]+1; i++) {
            bicsP1[is1.variables().get(i)] = b1;
            if (bs1.get(i) || i == n-1) {
                cutsP1[b1] = i;
                size1 = i - cutsP1[b1-1];
                if (b1 != 1 && size1 > maxSize1) maxSize1 = size1;
                if (b1 != 1 && size1 < minSize1) minSize1 = size1;
                b1 += 1;
            }
        }

        // For individual 2:
        int b2 = 1;
        int size2, maxSize2 = 0, minSize2 = n;
        int[] bicsP2 = new int[n];
        int[] cutsP2 = new int[numBicsP2 + 1];
        for (int i = limits2[0]+1; i < limits2[1]+1; i++) {
            bicsP2[is2.variables().get(i)] = b2;
            if (bs2.get(i) || i == n-1) {
                cutsP2[b2] = i;
                size2 = i - cutsP2[b2-1];
                if (b2 != 1 && size2 > maxSize2) maxSize2 = size2;
                if (b2 != 1 && size2 < minSize2) minSize2 = size2;
                b2 += 1;
            }
        }

        // In the matches matrix, the number of rows that the bicluster 'i' in the main individual and the bicluster 'j' in the complementary individual have in common are stored in position [i][j].
        int[][] matchesP1 = new int[numBicsP1+1][numBicsP2+1];
        int[][] matchesP2 = new int[numBicsP2+1][numBicsP1+1];
        for (int i = 0; i < n; i++) {
            matchesP1[bicsP1[i]][bicsP2[i]]++;
            matchesP2[bicsP2[i]][bicsP1[i]]++;
        }

        // For each bicluster, the complementary bicluster that has the most rows in common is finally chosen (excluding bicluster 0 with the remainder out of range).
        int[] bestMatchesP1 = getBestMatches(matchesP1);
        int[] bestMatchesP2 = getBestMatches(matchesP2);

        // The rows already added to the solution are stored in the visited vector to ensure the maintenance of the permutation. In this case, it is initialized by setting to true the rows that do not participate in the crossing and that remain outside the range
        // For individual 1:
        boolean[] visitedO1 = new boolean[n];
        for (int i = 0; i <= limits1[0]; i++) {
            visitedO1[is1.variables().get(i)] = true;
        }
        for (int i = limits1[1]+1; i < n; i++) {
            visitedO1[is1.variables().get(i)] = true;
        }

        // For individual 2:
        boolean[] visitedO2 = new boolean[n];
        for (int i = 0; i <= limits2[0]; i++) {
            visitedO2[is2.variables().get(i)] = true;
        }
        for (int i = limits2[1]+1; i < n; i++) {
            visitedO2[is2.variables().get(i)] = true;
        }

        // Copies of the genetic content of the parents are extracted in the ranges to be crossed
        int[] p1 = is1.variables().stream().skip(limits1[0]+1).limit(limits1[1] - limits1[0]).mapToInt(Integer::intValue).toArray();
        int[] p2 = is2.variables().stream().skip(limits2[0]+1).limit(limits2[1] - limits2[0]).mapToInt(Integer::intValue).toArray();

        // Reset the bits of the action zone and update the permutation by grouping matches
        bs1.clear(limits1[0]+1, limits1[1]+1);
        updateSolutions(is1, limits1[0]+1, limits2[0]+1, p1, p2, bs1, cutsP1, cutsP2, bestMatchesP1, visitedO1, doned, maxSize1, minSize1);
        bs2.clear(limits2[0]+1, limits2[1]+1);
        updateSolutions(is2, limits2[0]+1, limits1[0]+1, p2, p1, bs2, cutsP2, cutsP1, bestMatchesP2, visitedO2, doned, maxSize2, minSize2);
    }

    /**
     * This method determines the start and end positions of the biclusters that will be involved in the crossover process.
     * 
     * @param bs A BinarySet representing the biclusters cuts.
     * @param seed The starting position for the crossover within the BinarySet.
     * @param numBics The number of biclusters to include in the crossover.
     * @param n The total number of positions within the BinarySet.
     * @return An array of two integers specifying the start and end positions of the range to be crossed.
     */
    public int[] getLimits (BinarySet bs, int seed, int numBics, int n) {
        int[] res = new int[2];

        int nextStart;
        int nextEnd;
        res[0] = seed;
        res[1] = seed;
        // If the seed falls on a 1, when the first expansion is achieved on both sides, 2 biclusters are generated, therefore it is fine at 0
        // If the seed falls on 0, when the first expansion is achieved on both sides, a single bicluster is generated, so we start it at -1 so that when they are added it remains at 1
        int nb = bs.get(seed) ? 0 : -1;
        while (nb < numBics) {
            nextStart = bs.previousSetBit(res[0]-1);
            nextEnd = bs.nextSetBit(res[1]+1);
            if (nb % 2 == 0) {
                // If the previous bit set to true is other than -1, a bicluster has been added behind
                if (nextStart != -1) {
                    res[0] = nextStart;
                } 
                // If it is -1 for the first time it means that we have reached the first 1 or that the seed has fallen between the start and the first 1
                // If the seed has fallen between the beginning and the first 1, the first bicluster is added so as not to take only a part
                else if (res[0] == seed) {
                    res[0] = 0;
                } 
                // If the next bit is other than -1, a bicluster has been added ahead
                else if (nextEnd != -1) {
                    res[1] = nextEnd;
                }
                // If the next bit is -1, the seed has fallen between the end and the last 1
                else if (res[1] == seed) {
                    res[1] = n-1;
                }
            } else {
                // Same as before but giving priority to the queue
                if (nextEnd != -1) {
                    res[1] = nextEnd;
                } else if (res[1] == seed) {
                    res[1] = n-1;
                } else if (nextStart != -1) {
                    res[0] = nextStart;
                } else if (res[0] == seed) {
                    res[0] = 0;
                }
            }
            nb++;
        }

        return res;
    }
    
    /**
     * Identifies the best matching bicluster for each bicluster based on the number of common rows.
     * This method analyzes the matches matrix to find the bicluster with the highest number of common rows for each bicluster,
     * facilitating the crossover by identifying the best biclusters to match.
     * 
     * @param matches A 2D array representing the number of common rows between biclusters of two parents.
     * @return An array containing the indices of the best matching bicluster for each bicluster.
     */
    public int[] getBestMatches(int[][] matches) {
        int[] bestMatches = new int[matches.length];
        int max;
        for (int i = 1; i < matches.length; i++) {
            max = 0;
            for (int j = 1; j < matches[0].length; j++) {
                if (matches[i][j] >= max) {
                    max = matches[i][j];
                    bestMatches[i] = j;
                }
            }
        }
        return bestMatches;
    }

    /**
     * Updates the solutions with the crossed genetic material, taking into account the best matches and ensuring that
     * the resulting solution maintains permutation properties.
     * 
     * @param is The IntegerSolution to be updated with crossed genetic material.
     * @param start The starting position for the update in the solution.
     * @param startComp The starting position in the complementary parent solution.
     * @param p The genetic material from the parent within the crossover range.
     * @param pComp The genetic material from the complementary parent within the crossover range.
     * @param bs A BinarySet representing the genetic structure of the solution after crossover.
     * @param cuts The cut points for biclusters in the parent solution.
     * @param cutsComp The cut points for biclusters in the complementary parent solution.
     * @param bestMatches The indices of the best matching biclusters between the two parents.
     * @param visited An array tracking which rows have already been added to the solution.
     * @param doned The percentage of crossover operations completed.
     * @param maxSize The maximum bicluster size of the solution.
     * @param minSize The minimum bicluster size of the solution.
     */
    public void updateSolutions(IntegerSolution is, int start, int startComp, int[] p, int[] pComp, BinarySet bs, int[] cuts, int[] cutsComp, int[] bestMatches, boolean[] visited, float doned, int maxSize, int minSize) {
        int bm;
        int cut, prevCut = start;
        int cutComp, prevCutComp = 0;
        int cnt = 0;
        float r, growthFactor;
        int initialSize, numRows;
        ArrayList<Integer> rows = new ArrayList<>();
        for (int b = 1; b < bestMatches.length; b++){
            bm = bestMatches[b];
            cut = cuts[b];
            cutComp = cutsComp[bm];
            prevCutComp = bm == 1 ? startComp : cutsComp[bm-1];

            for (int j = prevCut - start; j <= cut - start; j++) {
                if (!visited[p[j]]) {
                    rows.add(p[j]);
                    visited[p[j]] = true;
                }
            }
            for (int j = prevCutComp - startComp; j <= cutComp - startComp; j++) {
                if (!visited[pComp[j]]) {
                    rows.add(pComp[j]);
                    visited[pComp[j]] = true;
                }
            }

            if (doned < shuffleEnd) Collections.shuffle(rows);
            numRows = rows.size();
            for (int j = 0; j < numRows; j++) {
                is.variables().set(start + cnt + j, rows.get(j));
            }

            r = random.nextFloat();
            initialSize = cut - prevCut + 1;
            growthFactor = (1 - (float) initialSize / numRows + (float) (initialSize - minSize) / Math.max(1, maxSize - minSize)) / 2;
            bs.set(start + cnt + numRows - 1);
            if (r < growthFactor * growthFactor) {
                bs.set(start + cnt + numRows/3 - 1);
                bs.set(start + cnt + 2*numRows/3 - 1);
            } else if (r < growthFactor) {
                bs.set(start + cnt + numRows/2 - 1);
            }
            cnt += numRows;
            prevCut = cut + 1;
            rows.clear();
        }
    }
    
}
