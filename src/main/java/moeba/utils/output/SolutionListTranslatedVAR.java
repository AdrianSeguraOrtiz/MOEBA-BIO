package moeba.utils.output;

import moeba.Representation;
import moeba.StaticUtils;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SolutionListTranslatedVAR {
    private Representation representation;
    private int numRows;
    private int numCols;

    public SolutionListTranslatedVAR(Representation representation, int numRows, int numCols) {
        this.representation = representation;
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public void printTranslatedVAR(String strFile, List<? extends Solution<?>> solutionList) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(strFile));
            for (Solution<?> solution : solutionList) {
                writeTranslatedSolution(bufferedWriter, solution);
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            throw new JMetalException("Error writing variables to file", e);
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                throw new JMetalException("Error closing the buffered writer", e);
            }
        }
    }

    public void writeTranslatedSolution(BufferedWriter bufferedWriter, Solution<?> solution) throws IOException {
        ArrayList<ArrayList<Integer>[]> biclusters = StaticUtils.getBiclustersFromRepresentation((CompositeSolution) solution, this.representation, this.numRows, this.numCols);
        for (int j = 0; j < biclusters.size(); j++) {
            ArrayList<Integer>[] bicArray = biclusters.get(j);
            String biclusterString = "Bicluster" + j + ": " + StaticUtils.biclusterToString(bicArray) + (j == biclusters.size() - 1 ? "" : ", ");
            bufferedWriter.write(biclusterString);
        }
    }
}
