package moeba.validation.metric.impl;

import java.util.ArrayList;
import java.util.Map;

import org.jgrapht.alg.matching.blossom.v5.KolmogorovWeightedPerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import moeba.validation.metric.MetricInterface;

/**
 * Implementation of the Clustering Error metric for evaluating bicluster similarity.
 * This metric calculates the error between sets of inferred biclusters and gold standard biclusters
 * based on a confusion matrix and a weighted perfect matching algorithm.
 * The Clustering Error is computed as the difference between the union of bicluster elements and the maximum diagonal sum
 * of the confusion matrix normalized by the union sum.
 * 
 * Based on: Patrikainen, A., & Meila, M. (2006). Comparing subspace clusterings. IEEE Transactions on Knowledge and Data Engineering, 18(7), 902-916.
 * 
 * This class extends MetricInterface and calculates the overall score based on this specific implementation.
 */
public class ClusteringErrorComplementary extends MetricInterface {

    /**
     * Constructor for ClusteringErrorComplementary.
     *
     * @param saveProcess boolean indicating whether the process should be saved. This option is not supported and will be ignored.
     * @param outputProcessFolder String representing the folder where the process output would be saved if supported.
     */
    public ClusteringErrorComplementary(boolean saveProcess, String outputProcessFolder) {
        super(saveProcess, outputProcessFolder);
        if (saveProcess) System.out.println("This metric (ClusteringErrorComplementary) doesnt support saving process, it will be ignored.");
    }

    /**
     * Calculates the Clustering Error score.
     * 
     * @param inferredBiclusters List of inferred biclusters, each represented by an array of two ArrayLists of integers.
     * @param goldStandardBiclusters List of gold standard biclusters, formatted similarly to inferred biclusters.
     * @param resultIndex Index of the result.
     * @return A double representing the Clustering Error score.
     */
    @Override
    protected double getScore(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        int[][] confusionMatrix = getConfusionMatrix(inferredBiclusters, goldStandardBiclusters);
        int dMax = calculateMaximumDiagonalSum(confusionMatrix);

        Map<String, Integer>[] nondisjointMaps = super.getNondisjointMaps(inferredBiclusters, goldStandardBiclusters);
        Map<String, Integer> nondisjointUnionMap = nondisjointMaps[0];
        int unionSum = 0;
        for (Map.Entry<String, Integer> entry : nondisjointUnionMap.entrySet()) {
            unionSum += entry.getValue();
        }

        return 1.0 - (double) (unionSum - dMax) / unionSum;
    }

    /**
     * Calculates the Clustering Error score and ignores saving the process.
     *
     * @param inferredBiclusters List of inferred biclusters, each bicluster represented by an array of two ArrayLists of integers.
     * @param goldStandardBiclusters List of gold standard biclusters, formatted similarly to inferred biclusters.
     * @param resultIndex Index of the result.
     * @return A double representing the Clustering Error score.
     */
    @Override
    protected double getScoreSavingProcess(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters, int resultIndex) {
        return getScore(inferredBiclusters, goldStandardBiclusters, resultIndex);
    }

    /**
     * Generates the confusion matrix for the given sets of inferred and gold standard biclusters.
     * 
     * @param inferredBiclusters List of inferred biclusters.
     * @param goldStandardBiclusters List of gold standard biclusters.
     * @return A 2D array representing the confusion matrix.
     */
    private int[][] getConfusionMatrix(ArrayList<ArrayList<Integer>[]> inferredBiclusters, ArrayList<ArrayList<Integer>[]> goldStandardBiclusters) {
        int matrixSize = Math.max(inferredBiclusters.size(), goldStandardBiclusters.size()); 
        int[][] confusionMatrix = new int[matrixSize][matrixSize];

        for (int i = 0; i < inferredBiclusters.size(); i++) {
            for (int j = 0; j < goldStandardBiclusters.size(); j++) {
                int[][] disjointIntersectionMatrix = getIntersectionOfPairs(getCartesianProduct(inferredBiclusters.get(i)[0], inferredBiclusters.get(i)[1]), getCartesianProduct(goldStandardBiclusters.get(j)[0], goldStandardBiclusters.get(j)[1]));
                confusionMatrix[i][j] = disjointIntersectionMatrix.length;
            }
        }

        return confusionMatrix;
    }

    /**
     * Calculates the maximum diagonal sum of the confusion matrix using a weighted perfect matching algorithm.
     * 
     * @param confusionMatrix The confusion matrix.
     * @return The maximum diagonal sum.
     */
    private int calculateMaximumDiagonalSum(int[][] confusionMatrix) {
        int size = confusionMatrix.length;
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph = createGraph(confusionMatrix, size);
        var matching = new KolmogorovWeightedPerfectMatching<>(graph);

        int maximumDiagonalSum = 0;
        for (DefaultWeightedEdge edge : matching.getMatching().getEdges()) {
            int sourceIndex = Integer.parseInt(graph.getEdgeSource(edge).substring(1));
            int targetIndex = Integer.parseInt(graph.getEdgeTarget(edge).substring(1));
            maximumDiagonalSum += confusionMatrix[sourceIndex][targetIndex];
        }

        return maximumDiagonalSum;
    }

    /**
     * Creates a weighted graph from the confusion matrix for use in the perfect matching algorithm.
     * 
     * @param confusionMatrix The confusion matrix.
     * @param size The size of the matrix.
     * @return A SimpleWeightedGraph representing the confusion matrix.
     */
    private SimpleWeightedGraph<String, DefaultWeightedEdge> createGraph(int[][] confusionMatrix, int size) {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < size; i++) {
            graph.addVertex("u" + i);
            graph.addVertex("v" + i);
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                DefaultWeightedEdge edge = graph.addEdge("u" + i, "v" + j);
                if (edge != null) {
                    graph.setEdgeWeight(edge, -confusionMatrix[i][j]);
                }
            }
        }
        return graph;
    }
}
