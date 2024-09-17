package moeba.fitnessfunction.impl;

import java.util.ArrayList;

import moeba.fitnessfunction.GenericBiclusterFitnessFunction;
import moeba.utils.storage.CacheStorage;

public class BiclusterSizeCoverNormComp extends GenericBiclusterFitnessFunction {
    private double rowsWeight;
    private double colsWeight;

    public BiclusterSizeCoverNormComp(double[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache,
            String summariseIndividualObjectives, double rowsWeight) {
        super(data, types, internalCache, summariseIndividualObjectives);
        this.rowsWeight = rowsWeight;
        this.colsWeight = 1 - rowsWeight;
    }

    @Override
    protected double getBiclusterScore(ArrayList<Integer>[] bicluster, ArrayList<ArrayList<Integer>[]> biclusters) {
        // Paso 1: Calcular el puntaje para las filas
        double rowScore = (double) bicluster[0].size() / data.length;

        // Paso 2: Crear un array que cuente cuántos biclusters contienen cada columna
        int[] columnCountArray = new int[data[0].length]; 

        // Recorrer todos los biclusters y contar las apariciones de cada columna
        for (ArrayList<Integer>[] otherBicluster : biclusters) {
            for (Integer col : otherBicluster[1]) {
                columnCountArray[col]++; 
            }
        }

        // Paso 3: Calcular el puntaje para las columnas, penalizando aquellas que están compartidas
        double colScore = 0.0;
        for (Integer col : bicluster[1]) {
            colScore += 1.0 / (columnCountArray[col] + 1);
        }

        // Normalizar el puntaje de las columnas
        colScore /= data[0].length;

        // Paso 4: Combinar los puntajes de filas y columnas con los pesos definidos
        return this.rowsWeight * rowScore + this.colsWeight * colScore;
    }
    
}
