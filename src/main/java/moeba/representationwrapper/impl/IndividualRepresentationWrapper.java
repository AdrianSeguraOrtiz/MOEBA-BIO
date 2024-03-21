package moeba.representationwrapper.impl;

import java.util.ArrayList;
import java.util.Arrays;

import moeba.representationwrapper.RepresentationWrapper;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.binarySet.BinarySet;

public class IndividualRepresentationWrapper extends RepresentationWrapper {

    public IndividualRepresentationWrapper(int numRows, int numColumns) {
        super(numRows, numColumns);
    }

    public CompositeSolution buildComposition(IntegerSolution integerSolution, BinarySolution binarySolution) {
        return new CompositeSolution(Arrays.asList(integerSolution, binarySolution));
    }

    @Override
    public int getNumIntVariables() {
        return 0;
    }

    @Override
    public int getNumBinaryVariables() {
        return 1;
    }

    @Override
    public int getLowerIntegerBound() {
        return 0;
    }

    @Override
    public int getUpperIntegerBound() {
        return 0;
    }

    @Override
    public int getNumBitsPerVariable() {
        return super.numRows + super.numColumns;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<ArrayList<Integer>[]> getBiclustersFromRepresentation(CompositeSolution solution) {
        // Initialize the result list
        ArrayList<ArrayList<Integer>[]> res = new ArrayList<>();
        
        // Extract binary variable from the composite solution
        BinarySet bs = ((BinarySolution) solution.variables().get(1)).variables().get(0);
        
        // Initialize bicluster, rows and cols
        ArrayList<Integer>[] bicluster = new ArrayList[2];
        ArrayList<Integer> rows = new ArrayList<>();
        ArrayList<Integer> cols = new ArrayList<>();

        // Iterate over rows and add them to the bicluster if they are set to true
        for (int i = 0; i < super.numRows; i++) {
            if (bs.get(i)) {
                rows.add(i);
            }
        }
        
        // Iterate over columns and add them to the bicluster if they are set to true
        for (int i = 0; i < super.numColumns; i++) {
            if (bs.get(i + super.numRows)) {
                cols.add(i);
            }
        }

        // Add rows and cols to the bicluster and add it to the result list
        bicluster[0] = rows;
        bicluster[1] = cols;
        res.add(bicluster);
        
        return res;
    }

    @Override
    public CrossoverOperator<CompositeSolution> getCrossoverFromString(String strCrossoverOperator,
            double crossoverProbability, int numApproxCrossovers) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCrossoverFromString'");
    }

    @Override
    public MutationOperator<CompositeSolution> getMutationFromString(String strMutationOperator,
            String mutationProbability, int numApproxMutations) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMutationFromString'");
    }
    
}
