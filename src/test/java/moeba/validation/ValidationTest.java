package moeba.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.annotations.Test;

import moeba.validation.metric.impl.ClusteringError;
public class ValidationTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void testDisjointClusteringError() {
        ArrayList<ArrayList<Integer>[]> goldStandardBiclusters = new ArrayList<>();
        ArrayList<Integer>[] gsb1 = new ArrayList[]{new ArrayList<>(Arrays.asList(0, 1)), new ArrayList<>(Arrays.asList(2, 3))};
        ArrayList<Integer>[] gsb2 = new ArrayList[]{new ArrayList<>(Arrays.asList(2, 3)), new ArrayList<>(Arrays.asList(2, 3))};
        ArrayList<Integer>[] gsb3 = new ArrayList[]{new ArrayList<>(Arrays.asList(5, 6)), new ArrayList<>(Arrays.asList(4, 5, 6, 7))};
        goldStandardBiclusters.add(gsb1);
        goldStandardBiclusters.add(gsb2);
        goldStandardBiclusters.add(gsb3);
        
        ArrayList<ArrayList<Integer>[]> inferredBiclusters = new ArrayList<>();
        ArrayList<Integer>[] ib1 = new ArrayList[]{new ArrayList<>(Arrays.asList(0, 1, 2, 3)), new ArrayList<>(Arrays.asList(2, 3))};
        ArrayList<Integer>[] ib2 = new ArrayList[]{new ArrayList<>(Arrays.asList(5, 6)), new ArrayList<>(Arrays.asList(3, 4))};
        ArrayList<Integer>[] ib3 = new ArrayList[]{new ArrayList<>(Arrays.asList(3, 4, 5)), new ArrayList<>(Arrays.asList(6, 7, 8))};
        inferredBiclusters.add(ib1);
        inferredBiclusters.add(ib2);
        inferredBiclusters.add(ib3);

        ClusteringError ce = new ClusteringError(false, null);
        assertEquals(ce.run(goldStandardBiclusters, inferredBiclusters, 0), 19.0/25.0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNonDisjointClusteringError() {
        ArrayList<ArrayList<Integer>[]> goldStandardBiclusters = new ArrayList<>();
        ArrayList<Integer>[] gsb1 = new ArrayList[]{new ArrayList<>(Arrays.asList(0, 1)), new ArrayList<>(Arrays.asList(2, 3))};
        ArrayList<Integer>[] gsb2 = new ArrayList[]{new ArrayList<>(Arrays.asList(2, 3)), new ArrayList<>(Arrays.asList(2, 3))};
        ArrayList<Integer>[] gsb3 = new ArrayList[]{new ArrayList<>(Arrays.asList(5, 6)), new ArrayList<>(Arrays.asList(4, 5, 6, 7))};
        ArrayList<Integer>[] gsb4 = new ArrayList[]{new ArrayList<>(Arrays.asList(1, 2)), new ArrayList<>(Arrays.asList(3, 4))};
        goldStandardBiclusters.add(gsb1);
        goldStandardBiclusters.add(gsb2);
        goldStandardBiclusters.add(gsb3);
        goldStandardBiclusters.add(gsb4);
        
        ArrayList<ArrayList<Integer>[]> inferredBiclusters = new ArrayList<>();
        ArrayList<Integer>[] ib1 = new ArrayList[]{new ArrayList<>(Arrays.asList(0, 1, 2, 3)), new ArrayList<>(Arrays.asList(2, 3))};
        ArrayList<Integer>[] ib2 = new ArrayList[]{new ArrayList<>(Arrays.asList(5, 6)), new ArrayList<>(Arrays.asList(3, 4))};
        ArrayList<Integer>[] ib3 = new ArrayList[]{new ArrayList<>(Arrays.asList(3, 4, 5)), new ArrayList<>(Arrays.asList(6, 7, 8))};
        ArrayList<Integer>[] ib4 = new ArrayList[]{new ArrayList<>(Arrays.asList(1)), new ArrayList<>(Arrays.asList(3, 4))};
        ArrayList<Integer>[] ib5 = new ArrayList[]{new ArrayList<>(Arrays.asList(1, 2)), new ArrayList<>(Arrays.asList(3))};
        inferredBiclusters.add(ib1);
        inferredBiclusters.add(ib2);
        inferredBiclusters.add(ib3);
        inferredBiclusters.add(ib4);
        inferredBiclusters.add(ib5);

        ClusteringError ce = new ClusteringError(false, null);
        assertEquals(ce.run(goldStandardBiclusters, inferredBiclusters, 0), 21.0/30.0);
    }
}
