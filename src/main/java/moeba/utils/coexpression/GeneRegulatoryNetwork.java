package moeba.utils.coexpression;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;

/**
 * The GeneRegulatoryNetwork class is responsible for inferring a gene regulatory network
 * using the GENIE3 algorithm within a Docker container. It processes gene expression data,
 * infers the network, and calculates network properties such as adjacency matrix, 
 * in-degrees, out-degrees, and total weight of the regulatory interactions.
 */
public class GeneRegulatoryNetwork {

    private float[][] adjRegMatrix; // Adjacency matrix representing the gene regulatory network
    private float[] inDegrees;      // Array representing the in-degree of each gene
    private float[] outDegrees;     // Array representing the out-degree of each gene
    private float totalWeight;      // Total weight of the regulatory interactions in the network

    /**
     * Constructor that initiates the process of inferring the gene regulatory network.
     * 
     * @param geneExpressionData A 2D double array where each row corresponds to the expression
     *                           levels of a gene and each column represents a sample or condition.
     */
    public GeneRegulatoryNetwork(double[][] geneExpressionData) {
        // Initialize Docker image information and create temporary directories for I/O
        String imageName = "adriansegura99/geneci_infer-network_genie3:2.0.0";
        Path tempDir = null;

        // Create a temporary directory for storing input/output files
        try {
            tempDir = Files.createTempDirectory("docker-temp");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Define input and output file paths
        Path inputFilePath = tempDir.resolve("input.csv");
        Path outputFilePath = tempDir.resolve("GRN_GENIE3_ET.csv");

        // Write the gene expression data to a CSV file
        writeExpressionDataToFile(geneExpressionData, inputFilePath.toFile());

        // Set up Docker client and infer the gene regulatory network using GENIE3
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        long startTime = System.currentTimeMillis();
        String containerId = runNetworkInference(dockerClient, imageName, tempDir, inputFilePath);
        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;
        System.out.println("Inference time: " + duration + " s");

        // Initialize adjacency matrix and degree arrays
        this.adjRegMatrix = new float[geneExpressionData.length][geneExpressionData.length];
        this.inDegrees = new float[geneExpressionData.length];
        this.outDegrees = new float[geneExpressionData.length];
        this.totalWeight = 0;

        // Read the output CSV file and build the adjacency matrix
        if (Files.exists(outputFilePath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(outputFilePath.toFile()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length == 3) {
                        int source = Integer.parseInt(values[0].substring(1)); // Extract source gene ID
                        int target = Integer.parseInt(values[1].substring(1)); // Extract target gene ID
                        Float confidence = Float.parseFloat(values[2]);        // Extract confidence value
                        this.adjRegMatrix[source][target] = confidence;
                        this.outDegrees[source] += confidence;
                        this.inDegrees[target] += confidence;
                        this.totalWeight += confidence;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Output file not found.");
        }

        // Remove Docker container after inference is completed
        dockerClient.removeContainerCmd(containerId).exec();

        // Clean up temporary files and directories
        try {
            Files.deleteIfExists(inputFilePath);
            Files.deleteIfExists(outputFilePath);
            Files.deleteIfExists(tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the gene expression data to a CSV file for input into the Docker container.
     * 
     * @param geneExpressionData 2D double array with gene expression data.
     * @param file File object representing the CSV file to write to.
     */
    private void writeExpressionDataToFile(double[][] geneExpressionData, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            for (int i = 0; i < geneExpressionData[0].length; i++) {
                writer.write("C" + i); // Write column headers for conditions
                if (i < geneExpressionData[0].length - 1) {
                    writer.write(",");
                }
            }
            writer.write("\n");

            for (int i = 0; i < geneExpressionData.length; i++) {
                writer.write("G" + i + ","); // Write row headers for genes
                for (int j = 0; j < geneExpressionData[i].length; j++) {
                    writer.write(String.valueOf(geneExpressionData[i][j]));
                    if (j < geneExpressionData[i].length - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes the GENIE3 algorithm in a Docker container to infer the gene regulatory network.
     * 
     * @param dockerClient The Docker client object for interacting with Docker.
     * @param imageName The name of the Docker image to use for inference.
     * @param tempDir Path to the temporary directory used for input/output files.
     * @param inputFilePath Path to the input CSV file containing gene expression data.
     * @return The ID of the Docker container running the inference.
     */
    private String runNetworkInference(DockerClient dockerClient, String imageName, Path tempDir, Path inputFilePath) {

        // Check if the Docker image is available locally, if not, pull it
        try {
            dockerClient.inspectImageCmd(imageName).exec();
        } catch (NotFoundException e) {
            try {
                dockerClient.pullImageCmd(imageName).exec(new ResultCallback.Adapter<PullResponseItem>() {}).awaitCompletion();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        // Create the Docker container for network inference
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withHostConfig(new HostConfig()
                        .withBinds(
                                new Bind(tempDir.toString(), new Volume("/usr/local/src/inferred_networks"))
                        ))
                .withCmd("inferred_networks/" + inputFilePath.getFileName().toString(), "inferred_networks", "ET")
                .exec();

        // Start the container
        dockerClient.startContainerCmd(container.getId()).exec();

        // Wait for the container to finish processing
        try {
            dockerClient.waitContainerCmd(container.getId()).exec(new WaitContainerResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return container.getId();
    }

    /**
     * Gets the confidence value (weight) of the regulatory interaction between a source and target gene.
     * 
     * @param source Index of the source gene.
     * @param target Index of the target gene.
     * @return The confidence score for the regulatory interaction.
     */
    public float getConfidence(int source, int target) {
        return this.adjRegMatrix[source][target];
    }

    /**
     * Gets the in-degree of a gene, representing the total incoming regulatory influence.
     * 
     * @param node Index of the gene.
     * @return The in-degree value of the gene.
     */
    public float getInDegree(int node) {
        return this.inDegrees[node];
    }

    /**
     * Gets the out-degree of a gene, representing the total outgoing regulatory influence.
     * 
     * @param node Index of the gene.
     * @return The out-degree value of the gene.
     */
    public float getOutDegree(int node) {
        return this.outDegrees[node];
    }

    /**
     * Gets the total weight of all regulatory interactions in the network.
     * 
     * @return The total weight of the network.
     */
    public float getTotalWeight() {
        return this.totalWeight;
    }

    /**
     * Gets the number of nodes in the network.
     * 
     * @return The number of nodes in the network.
     */
    public int getNumNodes() {
        return this.adjRegMatrix.length;
    }
}
