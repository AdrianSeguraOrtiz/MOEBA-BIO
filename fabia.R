library(fabia)
library(jsonlite)

# Función para generar y guardar los datos
generate_and_save_data <- function(instance_name, n, l, p, f1, f2, of1, of2, sd_noise, sd_z_noise, mean_z, sd_z, sd_l_noise, mean_l, sd_l) {
    # Generar datos simulados
    data <- makeFabiaData(n, l, p, f1, f2, of1, of2, sd_noise, sd_z_noise, mean_z, sd_z, sd_l_noise, mean_l, sd_l)

    # Convertir los datos de expresión con ruido a un archivo TSV
    csv_filename <- paste0("fabia_simulated_data/", instance_name, "_expression_data.tsv")
    write.table(data$X, file = csv_filename, sep = "\t", col.names=NA)

    # Crear una estructura JSON para los biclusters
    biclusters <- list()

    for (i in 1:length(data$LC)) {
        biclusters[[as.character(i - 1)]] <- list(
            X = data$LC[[i]],
            Y = data$ZC[[i]]
        )
    }

    # Convertir la lista de biclusters a JSON
    biclusters_json <- toJSON(list(biclusters = biclusters), pretty = TRUE)

    # Guardar el JSON en un archivo
    json_filename <- paste0("fabia_simulated_data/", instance_name, "_biclusters.json")
    write(biclusters_json, file = json_filename)

    # Unir todos los arrays en uno solo
    combined <- unlist(data$LC)

    # Contar las ocurrencias de cada número del 0 al 499
    counts <- table(factor(combined, levels = 0:n))

    # Clasificar los números según las ocurrencias
    no_aparecen <- sum(counts == 0)
    aparecen_una_vez <- sum(counts == 1)
    aparecen_mas_de_una_vez <- sum(counts > 1)

    # Imprimir los resultados
    cat("Instancia:", instance_name, "\n")
    cat("No agrupados:", no_aparecen / n * 100, "%\n")
    cat("Agrupados:", aparecen_una_vez / n * 100, "%\n")
    cat("Overlapped:", aparecen_mas_de_una_vez / n * 100, "%\n\n")
}

# Parámetros de las instancias
instances <- list(
    list(
        name = "instance1",
        description = "Biclusters Pequeños con Bajo Ruido",
        n = 200,
        l = 100,
        p = 10,
        f1 = 100,
        f2 = 200,
        of1 = 100 / 10,
        of2 = 200 / 10,
        sd_noise = 1.0,
        sd_z_noise = 0.1,
        mean_z = 2.0,
        sd_z = 0.5,
        sd_l_noise = 0.1,
        mean_l = 3.0,
        sd_l = 0.5
    ),
    list(
        name = "instance2",
        description = "Biclusters Grandes con Ruido Moderado",
        n = 500,
        l = 200,
        p = 8,
        f1 = 200,
        f2 = 500,
        of1 = 200 / 8,
        of2 = 500 / 8,
        sd_noise = 2.0,
        sd_z_noise = 0.2,
        mean_z = 2.5,
        sd_z = 1.0,
        sd_l_noise = 0.2,
        mean_l = 3.5,
        sd_l = 1.0
    ),
    list(
        name = "instance3",
        description = "Biclusters Mixtos con Alto Ruido",
        n = 300,
        l = 150,
        p = 12,
        f1 = 150 / 5,
        f2 = 300 / 5,
        of1 = (150 / 12) - 5,
        of2 = (300 / 12) - 5,
        sd_noise = 4.0,
        sd_z_noise = 0.4,
        mean_z = 3.0,
        sd_z = 1.5,
        sd_l_noise = 0.3,
        mean_l = 4.0,
        sd_l = 1.5
    )
)

# Generar y guardar datos para cada instancia
for (instance in instances) {
    generate_and_save_data(instance$name, instance$n, instance$l, instance$p, instance$f1, instance$f2, instance$of1, instance$of2, instance$sd_noise, instance$sd_z_noise, instance$mean_z, instance$sd_z, instance$sd_l_noise, instance$mean_l, instance$sd_l)
}
