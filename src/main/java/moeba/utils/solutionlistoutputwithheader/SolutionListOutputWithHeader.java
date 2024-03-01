package moeba.utils.solutionlistoutputwithheader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.fileoutput.FileOutputContext;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;

/**
 * This class extends SolutionListOutput to include headers in the output files,
 * making the files more readable and informative. It is designed for use with JMetal,
 * a framework for multi-objective optimization.
 */
public class SolutionListOutputWithHeader extends SolutionListOutput {
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
    public SolutionListOutputWithHeader(List<? extends Solution<?>> solutionList, String[] funLabels, String[] varLabels) {
        super(solutionList);
        this.funLabels = funLabels;
        this.varLabels = varLabels;
    }

    /**
     * Writes the variables of the solutions to a file, including a header with labels.
     *
     * @param context        The file output context that specifies the file to write to and its format.
     * @param solutionList   The list of solutions whose variables are to be written.
     */
    @Override
    public void printVariablesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
        BufferedWriter bufferedWriter = context.getFileWriter();

        try {
            if (!solutionList.isEmpty()) {
                // Write the header with variable labels
                bufferedWriter.write(String.join(",", varLabels));
                bufferedWriter.newLine();
                
                // Write variables for each solution
                int numberOfVariables = solutionList.get(0).variables().size();
                for (Solution<?> solution : solutionList) {
                    for (int j = 0; j < numberOfVariables; j++) {
                        bufferedWriter.write(solution.variables().get(j) + ((j < numberOfVariables - 1) ? context.getSeparator() : ""));
                    }
                    bufferedWriter.newLine();
                }
            }
        } catch (IOException e) {
            throw new JMetalException("Error writing variables to file", e);
        } finally {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                // Handle potential IOException on close
                throw new JMetalException("Error closing the buffered writer", e);
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
