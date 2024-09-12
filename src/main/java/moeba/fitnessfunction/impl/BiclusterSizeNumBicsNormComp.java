package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.GenericBiclusterFitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterSizeNumBicsNormComp extends GenericBiclusterFitnessFunction {
    private double rowsWeight;
    private double colsWeight;

    public BiclusterSizeNumBicsNormComp(double[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache,
            String summariseIndividualObjectives, double rowsWeight) {
        super(data, types, internalCache, summariseIndividualObjectives);
        this.rowsWeight = rowsWeight;
        this.colsWeight = 1 - rowsWeight;
    }

    @Override
    protected double getBiclusterScore(ArrayList<Integer>[] bicluster, ArrayList<ArrayList<Integer>[]> biclusters) {
        int numRows = data.length;
        int numCols = data[0].length;
        int numBiclusters = biclusters.size() + 1;

        double rowProportion = (double) bicluster[0].size() / numRows;
        double colProportion = (double) bicluster[1].size() / numCols;

        // Tamaño proporcional esperado para el bicluster
        double expectedProportion = 1.0 / numBiclusters;

        // Calcular la penalización basada en la desviación de la proporción esperada
        double rowPenalty = Math.pow(rowProportion - expectedProportion, 2);
        double colPenalty = Math.pow(colProportion - expectedProportion, 2);

        // Calcular el score combinando las penalizaciones
        double penalty = rowsWeight * rowPenalty + colsWeight * colPenalty;
        double score = 1 - penalty;

        return score;
    }
    
}
