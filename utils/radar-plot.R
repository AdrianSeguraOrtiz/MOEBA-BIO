library("ggplot2")
library("tidyr")
library("fmsb")


archivo1 <- "a_0.05.csv"
archivo2 <- "a_0.005.csv"
archivo3 <- "a_1e-05.csv"
df1 <- read.csv(archivo1)
df2 <- read.csv(archivo2)
df3 <- read.csv(archivo3)

# Función para procesar los resultados
procesar_resultados <- function(df) {
    rownames(df) <- df[, 1]
    df <- df[, -1]
    new_order <- c("YPD.2",
               "nitrogen.depletion",
               "complete.DTT",
               "heat.shock.2",
               "cdc.28",
               "heat.shock.1",
               "yeast.sporulation",
               "X1mM.menadione",
               "YPD.1",
               "cdc.15",
               "constant.32nM.H2O2",
               "X1.5mM.diamide",
               "alpha.factor",
               "elutriation",
               "X1M.sorbitol",
               "X2.5mM.DTT",
               "diauxic.shift")
    df <- df[, new_order]
    valores_maximos <- apply(df, 2, max)
    valores_minimos <- apply(df, 2, min)
    df <- rbind(valores_minimos, df)
    df <- rbind(valores_maximos, df)
    rownames(df) <- c("Max", "Min", tail(rownames(df), -2))
    return(df)
}

df1 <- procesar_resultados(df1)
df2 <- procesar_resultados(df2)
df3 <- procesar_resultados(df3)

# Establecer paleta de colores
colores <- c("#E54444", "#E59C44", "#D6E544", "#7FE544", "#44E562", 
             "#44B9E5", "#44E5B9", "#4462E5", "#7F44E5", "#D644E5", "#E5449C")

fill_colors <- scales::alpha(colores, 0.5)
fill_colors[-which(tail(rownames(df1), -2) == "Max MOEBA-BIO")] <- NA

# Guardar las gráficas en un archivo PDF
pdf(file = "radar.pdf", width = 15, height = 5)  # Aumenta la altura para acomodar la leyenda

# Crear gráfico de radar utilizando radarchart de fmsb
par(mai=c(0, 0.5, 0.25, 0.5), cex.main=2)
layout(matrix(c(1,2,3,4,4,4), ncol = 3, nrow = 2, byrow = TRUE), heights=c(3,1))
radarchart(
    df1,
    cglty = 1,       # Tipo de línea del grid
    cglcol = "gray", # Color del grid
    cglwd = 1,       # Ancho líneas grid
    pcol = colores,  # Color de la línea
    plwd = ifelse(tail(rownames(df1), -2) == "Max MOEBA-BIO", 3, 1),        # Ancho de la línea
    plty = 1,        # Tipo de línea
    pfcol = fill_colors,         # Color del área
    title = expression(alpha == 0.05),        # Título del radar
    vlcex = 1.4,  # Tamaño de las etiquetas de los ejes
    axistype = 4
)

radarchart(
    df2,
    cglty = 1,       # Tipo de línea del grid
    cglcol = "gray", # Color del grid
    cglwd = 1,       # Ancho líneas grid
    pcol = colores,  # Color de la línea
    plwd = ifelse(tail(rownames(df1), -2) == "Max MOEBA-BIO", 3, 1),        # Ancho de la línea
    plty = 1,        # Tipo de línea
    pfcol = fill_colors,         # Color del área
    title = expression(alpha == 0.005),        # Título del radar
    vlcex = 1.4,  # Tamaño de las etiquetas de los ejes
    axistype = 4
)

radarchart(
    df3,
    cglty = 1,       # Tipo de línea del grid
    cglcol = "gray", # Color del grid
    cglwd = 1,       # Ancho líneas grid
    pcol = colores,  # Color de la línea
    plwd = ifelse(tail(rownames(df1), -2) == "Max MOEBA-BIO", 3, 1),        # Ancho de la línea
    plty = 1,        # Tipo de línea
    pfcol = fill_colors,         # Color del área
    title = expression(alpha == 1e-05),        # Título del radar
    vlcex = 1.4,  # Tamaño de las etiquetas de los ejes
    axistype = 4
)

# Ajustar la leyenda
plot.new()
legend("top",
    legend = tail(rownames(df1), -2),
    bty = "n", pch = 20, col = colores,
    pt.cex = 4, ncol = 6, cex=1.4)

dev.off()  # Cerrar el archivo PDF
