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

/**
 * Responsible for writing a translated version of variable lists (VAR) from solutions to a file.
 * The translation process converts the solution representation into a more readable format,
 * specifically focusing on bicluster representations.
 */
public class SolutionListTranslatedVAR {
    private Representation representation;
    private int numRows;
    private int numCols;

    /**
     * Constructor to initialize the class with specific problem dimensions and representation.
     *
     * @param representation The representation scheme used for solutions.
     * @param numRows The number of rows in the representation matrix.
     * @param numCols The number of columns in the representation matrix.
     */
    public SolutionListTranslatedVAR(Representation representation, int numRows, int numCols) {
        this.representation = representation;
        this.numRows = numRows;
        this.numCols = numCols;
    }

    /**
     * Writes the translated variables of a list of solutions to a specified file.
     * This method iterates over each solution, translating and writing its content.
     *
     * @param strFile The file to write the translated variables to.
     * @param solutionList A list of solutions to be translated and written.
     * @throws JMetalException if there's an error during writing or closing the file.
     */
    public void printTranslatedVAR(String strFile, List<? extends Solution<?>> solutionList) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(strFile));
            // Iterate over each solution, writing its translated variables.
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
                // Handle potential IOException on closing the buffered writer.
                throw new JMetalException("Error closing the buffered writer", e);
            }
        }
    }

    /**
     * Writes a single solution's translated variables to the buffered writer.
     * This method extracts biclusters from the solution and formats them into a
     * readable string representation, detailing rows and columns involved in each bicluster.
     *
     * @param bufferedWriter The BufferedWriter instance used for writing to the file.
     * @param solution The solution whose translated variables are being written.
     * @throws IOException if an error occurs during file writing.
     */
    public void writeTranslatedSolution(BufferedWriter bufferedWriter, Solution<?> solution) throws IOException {
        // Extract biclusters from the solution representation.
        ArrayList<ArrayList<Integer>[]> biclusters = StaticUtils.getBiclustersFromRepresentation((CompositeSolution) solution, this.representation, this.numRows, this.numCols);
        for (int j = 0; j < biclusters.size(); j++) {
            ArrayList<Integer>[] bicArray = biclusters.get(j);
            // Write the bicluster number and its rows.
            bufferedWriter.write("Bicluster " + j + " (rows: [");
            for (int i = 0; i < bicArray[0].size(); i++) {
                bufferedWriter.write(bicArray[0].get(i).toString());
                if (i < bicArray[0].size() - 1) { // Add a comma unless it's the last element.
                    bufferedWriter.write(", ");
                }
            }
            // Write the bicluster's columns.
            bufferedWriter.write("], cols: [");
            for (int i = 0; i < bicArray[1].size(); i++) {
                bufferedWriter.write(bicArray[1].get(i).toString());
                if (i < bicArray[1].size() - 1) { // Add a comma unless it's the last element.
                    bufferedWriter.write(", ");
                }
            }
            bufferedWriter.write("]); ");
        }
    }
}
