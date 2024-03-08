package moeba.utils.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.fileoutput.FileOutputContext;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;

/**
 * This class extends SolutionListOutput to include headers in the output files,
 * making the files more readable and informative. It is designed for use with JMetal,
 * a framework for multi-objective optimization.
 */
public class SolutionListVARWithHeader extends SolutionListOutput {
    // Array containing the formulas or descriptions of the fitness functions
    private String[] funLabels;
    
    // Array containing labels for the variables to be included in the header of the variables output file
    private String[] varLabels;

    /**
     * Constructs a new SolutionListOutputWithHeader with specified solutions, fitness formulas, and VAR labels.
     *
     * @param solutionList    The list of solutions to be output.
     * @param funLabels       Descriptions or formulas of the fitness functions, used as headers in the objectives file.
     * @param varLabels       Labels for the solution variables, used as headers in the variables file.
     */
    public SolutionListVARWithHeader(List<? extends Solution<?>> solutionList, String[] funLabels, String[] varLabels) {
        super(solutionList);
        this.funLabels = funLabels;
        this.varLabels = varLabels;
    }

    @Override
    /**
     * Writes the variables of a list of solutions to a file using a specified file output context.
     * This method handles both simple and composite solutions, ensuring their variables are
     * appropriately recorded with the correct formatting.
     *
     * @param context The file output context, encapsulating file writing configurations.
     * @param solutionList A list of solutions whose variables are to be written to the file.
     * @throws JMetalException if there's an error during writing or closing the file.
     */
    public void printVariablesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
        BufferedWriter bufferedWriter = context.getFileWriter();
    
        try {
            if (!solutionList.isEmpty()) {
                // Write the header with variable labels
                bufferedWriter.write(String.join(",", varLabels));
                bufferedWriter.newLine();
                
                // Write variables for each solution in the list.
                for (Solution<?> solution : solutionList) {
                    writeSolution(bufferedWriter, solution, context);
                }
            }
        } catch (IOException e) {
            throw new JMetalException("Error writing variables to file", e);
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                // Handle potential IOException on closing the buffered writer.
                throw new JMetalException("Error closing the buffered writer", e);
            }
        }
    }
    
    /**
     * Writes variables of a single solution to the buffered writer.
     * Distinguishes between composite and simple solutions to handle their
     * variables accordingly.
     *
     * @param bufferedWriter The BufferedWriter instance used for writing to the file.
     * @param solution The solution whose variables are to be written.
     * @param context The file output context, providing formatting details such as separators.
     * @throws IOException if an error occurs during file writing.
     */
    private void writeSolution(BufferedWriter bufferedWriter, Solution<?> solution, FileOutputContext context) throws IOException {
        if (solution.variables().get(0) instanceof Solution) {
            // Handle composite solutions by iterating through sub-solutions.
            List<?> variables = solution.variables();
            for (int k = 0; k < variables.size(); k++) {
                Solution<?> subSolution = (Solution<?>) variables.get(k);
                writeVariablesOfSolution(bufferedWriter, subSolution, context, k == variables.size() - 1);
            }
        } else {
            // Handle simple solutions directly.
            writeVariablesOfSolution(bufferedWriter, solution, context, true);
        }
        bufferedWriter.newLine();
    }
    
    /**
     * Writes the variables of a solution to the buffered writer, formatting
     * according to the solution's variable type (e.g., BinarySet).
     *
     * @param bufferedWriter The BufferedWriter instance for writing.
     * @param solution The solution whose variables are being written.
     * @param context The file output context, providing formatting details.
     * @param isLast Indicates whether this is the last variable to avoid trailing separators.
     * @throws IOException if an error occurs during writing.
     */
    private void writeVariablesOfSolution(BufferedWriter bufferedWriter, Solution<?> solution, FileOutputContext context, boolean isLast) throws IOException {
        List<?> variables = solution.variables();
        for (int j = 0; j < variables.size(); j++) {
            Object variable = variables.get(j);
            if (variable instanceof BinarySet) {
                // Special handling for BinarySet variables.
                writeBinarySet(bufferedWriter, (BinarySet) variable);
            } else {
                // Default handling for other variable types.
                bufferedWriter.write(variable.toString());
            }
            if (!(j == variables.size() - 1 && isLast)) {
                bufferedWriter.write(context.getSeparator());
            }
        }
    }
    
    /**
     * Writes a BinarySet variable to the buffered writer, converting binary values to a
     * string representation ("1" for true, "0" for false).
     *
     * @param bufferedWriter The BufferedWriter instance for writing.
     * @param binarySet The BinarySet variable to be written.
     * @throws IOException if an error occurs during writing.
     */
    private void writeBinarySet(BufferedWriter bufferedWriter, BinarySet binarySet) throws IOException {
        for (int i = 0; i < binarySet.getBinarySetLength(); i++) {
            bufferedWriter.write(binarySet.get(i) ? "1" : "0");
            if (i != binarySet.getBinarySetLength() - 1) {
                bufferedWriter.write(",");
            }
        }
    }

    /**
     * Writes the objectives of the solutions to a file, including a header with fitness formulas or descriptions.
     *
     * @param context        The file output context that specifies the file to write to and its format.
     * @param solutionList   The list of solutions whose objectives are to be written.
     */
    @Override
    public void printObjectivesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            if (!solutionList.isEmpty()) {
                // Write the header with fitness function descriptions/formulas
                bufferedWriter.write(String.join(",", funLabels));
                bufferedWriter.newLine();
                
                // Write objectives for each solution
                int numberOfObjectives = solutionList.get(0).objectives().length;
                for (Solution<?> solution : solutionList) {
                    for (int j = 0; j < numberOfObjectives; j++) {
                        bufferedWriter.write(solution.objectives()[j] + ((j < numberOfObjectives - 1) ? context.getSeparator() : ""));
                    }
                    bufferedWriter.newLine();
                }
            }
        } catch (IOException e) {
            throw new JMetalException("Error printing objectives to file", e);
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                // Handle potential IOException on close
                throw new JMetalException("Error closing the buffered writer", e);
            }
        }
    }
}
