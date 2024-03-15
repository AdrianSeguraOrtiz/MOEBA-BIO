package moeba.operator.crossover.rowbiclustermixed.impl;

import java.util.BitSet;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

import moeba.operator.crossover.rowbiclustermixed.RowBiclusterMixedCrossover;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

public class GroupedBasedCrossover implements RowBiclusterMixedCrossover {
    private Random random;
    private AtomicInteger numOperations;
    private int numApproxCrossovers;
    private float shuffleEnd;
    private float dynamicStartAmount;

    public GroupedBasedCrossover(int numApproxCrossovers, float shuffleEnd, float dynamicStartAmount) {
        this.numApproxCrossovers = numApproxCrossovers;
        this.shuffleEnd = shuffleEnd;
        this.dynamicStartAmount = dynamicStartAmount;
        this.random = new Random();
        this.numOperations = new AtomicInteger();
    }

    public GroupedBasedCrossover(int numApproxCrossovers, float shuffleEnd, float dynamicStartAmount, Random random) {
        this.numApproxCrossovers = numApproxCrossovers;
        this.shuffleEnd = shuffleEnd;
        this.dynamicStartAmount = dynamicStartAmount;
        this.random = random;
        this.numOperations = new AtomicInteger();
    }

    @Override
    public void execute(IntegerSolution is1, IntegerSolution is2, BitSet bs1, BitSet bs2) {
        
        float doned = (float) numOperations.getAndIncrement() / this.numApproxCrossovers;
        float amount = (1 - dynamicStartAmount) * doned + dynamicStartAmount;

        int n = is1.variables().size();
        bs1.set(n-1);
        bs2.set(n-1);
        int numBicsP1 = bs1.cardinality();
        int numBicsP2 = bs2.cardinality();

        int[] bicsP1 = new int[n];
        int[] bicsP2 = new int[n];

        int b1 = 0;
        int b2 = 0;
        int[] cutsP1 = new int[numBicsP1];
        int[] cutsP2 = new int[numBicsP2];
        for (int i = 0; i < n; i++) {
            bicsP1[is1.variables().get(i)] = b1;
            bicsP2[is2.variables().get(i)] = b2;
            if (bs1.get(i)) {
                cutsP1[b1] = i;
                b1 += 1;
            }
            if (bs2.get(i)) {
                cutsP2[b2] = i;
                b2 += 1;
            }
        }

        int[][] matchesP1 = new int[numBicsP1][numBicsP2];
        int[][] matchesP2 = new int[numBicsP2][numBicsP1];

        for (int i = 0; i < n; i++) {
            matchesP1[bicsP1[i]][bicsP2[i]]++;
            matchesP2[bicsP2[i]][bicsP1[i]]++;
        }

        int[] bestMatchesP1 = getBestMatches(matchesP1);
        int[] bestMatchesP2 = getBestMatches(matchesP2);

        boolean[] visitedO1 = new boolean[is1.variables().size()];
        boolean[] visitedO2 = new boolean[is2.variables().size()];
        int[] p1 = is1.variables().stream().mapToInt(Integer::intValue).toArray();
        int[] p2 = is2.variables().stream().mapToInt(Integer::intValue).toArray();

        updateSolutions(is1, p1, p2, bs1, cutsP1, cutsP2, bestMatchesP1, visitedO1, doned);
        updateSolutions(is2, p2, p1, bs2, cutsP2, cutsP1, bestMatchesP2, visitedO2, doned);
    }

    public int[] getBestMatches(int[][] matches) {
        int[] bestMatches = new int[matches.length];
        int max, sum;
        for (int i = 0; i < matches.length; i++) {
            max = 0;
            sum = 0;
            for (int j = 0; j < matches[0].length; j++) {
                sum += matches[i][j];
                if (matches[i][j] > max) {
                    max = matches[i][j];
                    bestMatches[i] = j;
                }
                if (max > (matches[0].length - sum) / 2) {
                    break;
                }
            }
        }
        return bestMatches;
    }

    public void updateSolutions(IntegerSolution is, int[] p, int[] pComp, BitSet bs, int[] cuts, int[] cutsComp, int[] bestMatches, boolean[] visited, float doned) {
        bs.clear();
        int bm;
        int cut, prevCut = 0;
        int cutComp, prevCutComp = 0;
        int cnt = 0;
        float r;
        int numRows;
        ArrayList<Integer> rows = new ArrayList<>();
        for (int b = 0; b < bestMatches.length; b++){
            bm = bestMatches[b];
            cut = cuts[b];
            cutComp = cutsComp[bm];
            prevCutComp = bm == 0 ? 0 : cutsComp[bm-1];

            for (int j = prevCut; j <= cut; j++) {
                if (!visited[p[j]]) {
                    rows.add(p[j]);
                    visited[p[j]] = true;
                }
            }
            for (int j = prevCutComp; j <= cutComp; j++) {
                if (!visited[pComp[j]]) {
                    rows.add(pComp[j]);
                    visited[pComp[j]] = true;
                }
            }

            if (doned < shuffleEnd) Collections.shuffle(rows);
            numRows = rows.size();
            for (int j = 0; j < numRows; j++) {
                is.variables().set(cnt + j, rows.get(j));
            }
            r = random.nextFloat();
            bs.set(cnt + numRows);
            if (r < 1/3f) {
                bs.set(cnt + numRows/3);
                bs.set(cnt + 2*numRows/3);
            } else if (r < 2/3f) {
                bs.set(cnt + numRows/2);
            }
            cnt += numRows;
            prevCut = cut + 1;
            rows.clear();
        }
    }
    
}
