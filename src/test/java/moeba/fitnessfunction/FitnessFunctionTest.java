package moeba.fitnessfunction;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.annotations.Test;

import moeba.fitnessfunction.impl.BiclusterSizeNormComp;
import moeba.fitnessfunction.impl.BiclusterSizeWeightedNormComp;
import moeba.fitnessfunction.impl.BiclusterVarianceNorm;
import moeba.fitnessfunction.impl.MeanSquaredResidue;
import moeba.fitnessfunction.impl.RowVariance;

public class FitnessFunctionTest {

    final double[][] data = new double[][] {
        {0.5, 0.2, 0.7, 0.3},
        {0.1, 0.4, 0.6, 0.8},
        {0.7, 0.8, 0.5, 0.1},
        {0.2, 0.3, 0.8, 0.6}
    };
    final Class<?>[] types = new Class<?>[] {Float.class, Float.class, Float.class, Float.class};
    final double epsilon = 0.01;
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBiclusterSizeNormCompOneBicluster() {
        FitnessFunction f = new BiclusterSizeNormComp(data, types, null, "Mean");

        ArrayList<ArrayList<Integer>[]> biclusters = new ArrayList<>();
        ArrayList<Integer>[] b = new ArrayList[]{new ArrayList<>(Arrays.asList(1, 2)), new ArrayList<>(Arrays.asList(0, 1))};
        biclusters.add(b);

        assert(Math.abs((1.0 - 4.0/16.0) - f.run(biclusters)) < epsilon);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBiclusterSizeNormCompTwoBiclustersMean() {
        FitnessFunction f = new BiclusterSizeNormComp(data, types, null, "Mean");

        ArrayList<ArrayList<Integer>[]> biclusters = new ArrayList<>();
        ArrayList<Integer>[] b1 = new ArrayList[]{new ArrayList<>(Arrays.asList(1, 2)), new ArrayList<>(Arrays.asList(0, 1))};
        ArrayList<Integer>[] b2 = new ArrayList[]{new ArrayList<>(Arrays.asList(0, 2, 3)), new ArrayList<>(Arrays.asList(1, 3))};
        biclusters.add(b1);
        biclusters.add(b2);

        assert(Math.abs((1.0 - (4.0/16.0 + 6.0/16.0)/2) - f.run(biclusters)) < epsilon);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBiclusterSizeNormCompTwoBiclustersHarmonicMean() {
        FitnessFunction f = new BiclusterSizeNormComp(data, types, null, "HarmonicMean");

        ArrayList<ArrayList<Integer>[]> biclusters = new ArrayList<>();
        ArrayList<Integer>[] b1 = new ArrayList[]{new ArrayList<>(Arrays.asList(1, 2)), new ArrayList<>(Arrays.asList(0, 1))};
        ArrayList<Integer>[] b2 = new ArrayList[]{new ArrayList<>(Arrays.asList(0, 2, 3)), new ArrayList<>(Arrays.asList(1, 3))};
        biclusters.add(b1);
        biclusters.add(b2);

        assert(Math.abs((1.0 - 2.0/(1.0/(4.0/16.0) + 1.0/(6.0/16.0))) - f.run(biclusters)) < epsilon);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBiclusterSizeNormCompTwoBiclustersGeometricMean() {
        FitnessFunction f = new BiclusterSizeNormComp(data, types, null, "GeometricMean");

        ArrayList<ArrayList<Integer>[]> biclusters = new ArrayList<>();
        ArrayList<Integer>[] b1 = new ArrayList[]{new ArrayList<>(Arrays.asList(1, 2)), new ArrayList<>(Arrays.asList(0, 1))};
        ArrayList<Integer>[] b2 = new ArrayList[]{new ArrayList<>(Arrays.asList(0, 2, 3)), new ArrayList<>(Arrays.asList(1, 3))};
        biclusters.add(b1);
        biclusters.add(b2);

        assert(Math.abs((1.0 - Math.sqrt((4.0/16.0) * (6.0/16.0))) - f.run(biclusters)) < epsilon);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBiclusterSizeWeightedNormCompOneBicluster() {
        FitnessFunction f = new BiclusterSizeWeightedNormComp(data, types, null, "Mean");

        ArrayList<ArrayList<Integer>[]> biclusters = new ArrayList<>();
        ArrayList<Integer>[] b = new ArrayList[]{new ArrayList<>(Arrays.asList(1, 2, 3)), new ArrayList<>(Arrays.asList(1))};
        biclusters.add(b);

        assert(Math.abs((1.0 - (0.75*(3.0/4.0) + 0.25*(1.0/4.0))) - f.run(biclusters)) < epsilon);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBiclusterVarianceNormOneBicluster() {
        FitnessFunction f = new BiclusterVarianceNorm(data, types, null, "Mean");

        ArrayList<ArrayList<Integer>[]> biclusters = new ArrayList<>();
        ArrayList<Integer>[] b = new ArrayList[]{new ArrayList<>(Arrays.asList(2, 3)), new ArrayList<>(Arrays.asList(2, 3))};
        biclusters.add(b);

        assert(Math.abs((1.0 - (1.0 - 4 * (Math.pow(0.5-0.5, 2) + Math.pow(0.1-0.5, 2) + Math.pow(0.8-0.5, 2) + Math.pow(0.6-0.5, 2)) / 16.0)) - f.run(biclusters)) < epsilon);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRowVarianceOneBicluster() {
        FitnessFunction f = new RowVariance(data, types, null, "Mean");

        ArrayList<ArrayList<Integer>[]> biclusters = new ArrayList<>();
        ArrayList<Integer>[] b = new ArrayList[]{new ArrayList<>(Arrays.asList(2, 3)), new ArrayList<>(Arrays.asList(2, 3))};
        biclusters.add(b);

        assert(Math.abs((1.0 - (4 * (Math.pow(0.5-0.3, 2) + Math.pow(0.1-0.3, 2) + Math.pow(0.8-0.7, 2) + Math.pow(0.6-0.7, 2)) / 4.0)) - f.run(biclusters)) < epsilon);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMSROneBicluster() {
        FitnessFunction f = new MeanSquaredResidue(data, types, null, "Mean");

        ArrayList<ArrayList<Integer>[]> biclusters = new ArrayList<>();
        ArrayList<Integer>[] b = new ArrayList[]{new ArrayList<>(Arrays.asList(2, 3)), new ArrayList<>(Arrays.asList(2, 3))};
        biclusters.add(b);

        assert(Math.abs((1.0 - (1.0 - (Math.pow(0.5-0.3-0.65+0.5, 2) + Math.pow(0.1-0.3-0.35+0.5, 2) + Math.pow(0.8-0.7-0.65+0.5, 2) + Math.pow(0.6-0.7-0.35+0.5, 2) / 4.0) / 4.0)) - f.run(biclusters)) < epsilon);
    }
}
