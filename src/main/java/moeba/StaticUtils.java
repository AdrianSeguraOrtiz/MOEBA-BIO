package moeba;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import moeba.fitnessfunction.FitnessFunction;
import moeba.fitnessfunction.impl.BiclusterSize;

public final class StaticUtils {
    
    public static FitnessFunction getFitnessFunctionFromString(String str, Object [][] data, Class<?>[] types, Representation representation) {
        /** 
         * Function to return a basic FitnessFunction object based on a identifier string 
         */
        
        FitnessFunction res;
        switch (str.toLowerCase()) {

            case "biclustersize":
                res = new BiclusterSize(data, types, representation);
                break;
            /**
            case "biclustervariance":
                res = new BiclusterVariance(data, types);
                break;
            case "biclusterrowvariance":
                res = new BiclusterRowVariance(data, types);
                break;
            case "meansquaredresidue":
                res = new MeanSquaredResidue(data, types);
                break;
            case "scalingmeansquaredresidue":
                res = new ScalingMeanSquaredResidue(data, types);
                break;
            case "averagecorrelationfunction":
                res = new AverageCorrelationFunction(data, types);
                break;
            case "averagecorrelationvalue":
                res = new AverageCorrelationValue(data, types);
                break;
            case "virtualerror":
                res = new VirtualError(data, types);
                break;
            case "coefficientofvariationfunction":
                res = new CoefficientOfVariationFunction(data, types);
                break;
            */
            default:
                throw new RuntimeException("The fitness function " + str + " is not implemented.");
        }

        return res;
    }


    public static Object[][] csvToObjectMatrix(File inputDataset) throws IOException {
        // Read all lines from the CSV file sequentially
        List<String> lines = Files.readAllLines(inputDataset.toPath());
        
        // Process lines in parallel to convert them to an array of objects
        Object[][] matrix = lines.parallelStream()
            .skip(1) // Ignore the first line as it contains the heading
            .map(line -> line.split(",")) // Assumes comma as CSV separator
            .map(array -> (Object[]) array) // Direct conversion of String[] to Object[]
            .collect(Collectors.toList()) // Collect results in a list
            .toArray(new Object[0][]); // Convert list to a bidimensional array of objects
        
        return matrix;
    }

    public static Class<?>[] jsonToClassArray(File inputColumnTypes, String[] columnNames) throws IOException {
        // TODO
        return null;
    }
}
