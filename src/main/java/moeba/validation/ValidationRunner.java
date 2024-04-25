package moeba.validation;

import java.io.File;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "ValidationRunner", description = "Validation runner", mixinStandardHelpOptions = true, showDefaultValues = true, sortOptions = false)
public class ValidationRunner {

    @Option(names = {"--inferred-translated"}, description = "Path to the input CSV file with inferred translated biclusters", required = true)
    private File inferredTranslatedFile;

    @Option(names = {"--gold-standard-translated"}, description = "Path to the input CSV file with gold standard translated biclusters", required = true)
    private File goldStandardTranslatedFile;

    @Option(names = {"--validation-metrics"}, description = "Validation metrics separated by semicolon. Possible values: ScorePrelicRelevance, ScorePrelicRecovery", required = true)
    private String strValidationMetrics;

    @Option(names = {"--output-file"}, description = "Path to the output CSV file", required = true)
    private File outputFile;

    @Override
    public void run() {

        // 1. Obtener la lista de listas de biclusters de la inferencia

        // 2. Obtener la lista de biclusters del gold standard

        // 3. Obtener la lista de metricas de validación a partir del parametro en string

        // 4. Ejecutar las metricas de validación para cada solución del frente respecto al gold standard

        // 5. Guardar los resultados en un archivo CSV  

    }


    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new ValidationRunner());
        commandLine.execute(args);
    }
    
}
