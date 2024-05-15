import argparse
import csv
import re
import zipfile
from pathlib import Path

import networkx as nx
import numpy as np
import pandas as pd
import plotly.express as px
from matplotlib import pyplot as plt
from matplotlib.cm import viridis
from matplotlib.colors import Normalize
from matplotlib.patches import Circle, Wedge
from pyvis.network import Network


def csv_to_nested_dict(content):
    """
    Convert a CSV file content to a nested dictionary.

    Args:
        content (iterable): The content of the CSV file.

    Returns:
        dict: A nested dictionary where the keys are the column names
              and the values are dictionaries with the row names as keys
              and the corresponding values as values.
    """
    # Create an empty nested dictionary
    nested_dict = {}

    # Create a CSV reader
    csv_reader = csv.reader(content)
    
    # Extract the headers
    headers = next(csv_reader)[1:]  # Exclude the first empty column
    
    # Initialize the nested dictionary with dictionary comprehension
    nested_dict = {f"gs-{i}": {} for i in range(len(headers))}
    
    # Fill the nested dictionary with the CSV data
    for i, row in enumerate(csv_reader):
        for j, cell in enumerate(row[1:]):
            nested_dict[f"gs-{j}"][f"r-{i}"] = float(cell)
                
    return nested_dict


def parse_bicluster(line, suffix):
    """
    Parses a line of text and extracts biclusters information.

    Args:
        line (iterable): An iterable of strings containing the bicluster information.
        suffix (str): A string to be appended to the bicluster keys.

    Returns:
        dict: A dictionary where the keys are the biclusters IDs and the values are
              dictionaries with the rows and columns information.
    """
    # Initialize an empty dictionary to store the biclusters
    biclusters = {}

    # Define a pattern to match the bicluster information
    pattern = r' ?Bicluster\d+: \(rows: \[(.*?)\] cols: \[(.*?)\]\)'

    # Counter to keep track of the biclusters
    cnt = 0

    # Iterate over each column in the line
    for col in line:
        # Skip if the column is NaN
        if not pd.isna(col):
            # Try to match the column with the bicluster pattern
            match = re.match(pattern, col)
            # If a match is found
            if match:
                # Extract the rows and columns information
                rows = list(map(int, match.group(1).split()))
                cols = list(map(int, match.group(2).split()))
                # Create a key for the bicluster with the suffix and the counter
                key = f'{suffix}{cnt}'
                # Store the rows and columns information in the dictionary
                biclusters[key] = {'rows': rows, 'cols': cols}
                # Increment the counter
                cnt += 1

    # Return the dictionary with the biclusters information
    return biclusters


def generate_colors(n, colormap):
    """
    Generate a list of colors for a given number of biclusters.

    Args:
        n (int): The number of biclusters.
        colormap (matplotlib.cm.Colormap): The colormap to use for generating the colors.

    Returns:
        list: A list of colors, where each color is a tuple (r, g, b, a).
    """
    # Normalize the index of each bicluster to be between 0 and 1
    norm = Normalize(vmin=0, vmax=max(n, 1))  # Avoid division by zero if n is 0

    # Generate the colors using the colormap and normalized indices
    return [colormap(norm(i)) for i in range(n)]


def create_image_matrix(n, m, cells, color):
    """
    Create an image matrix with the given dimensions and fill it with the given color.

    Args:
        n (int): The number of rows in the matrix.
        m (int): The number of columns in the matrix.
        cells (dict): Dictionary containing the rows and columns of the cells to be colored.
        color (tuple): Tuple of RGB values to color the cells.

    Returns:
        numpy.ndarray: Matrix of shape (n, m, 4) with the colored cells.
    """
    # Create a matrix of ones with the given dimensions and an extra channel for the alpha value
    image = np.ones((n, m, 4))

    # Iterate over the rows and columns of the cells to be colored
    for row in cells["rows"]:
        for col in cells["cols"]:
            # Set the color of the cell
            image[row, col, :] = color

    # Return the matrix with the colored cells
    return image

# Función para añadir círculos al gráfico
def add_circles_to_plot(ax, n, m, biclusters, colors, filtered_image, rmask, cmask):
    """
    Add circles to a plot based on the given parameters.

    Args:
        ax (matplotlib.axes.Axes): The plot axes.
        n (int): The number of rows in the matrix.
        m (int): The number of columns in the matrix.
        biclusters (list): A list of biclusters, where each bicluster is a tuple containing the index and the cells.
        colors (list): A list of colors, where each color is a tuple (r, g, b, a).
        filtered_image (numpy.ndarray): The filtered image matrix.
        rmask (list): A list of booleans indicating which rows are selected.
        cmask (list): A list of booleans indicating which columns are selected.
    """
    radius = 0.5  # radius of the circles
    for row in range(n):
        for col in range(m):
            # Check if the current cell is selected
            if rmask[row] and cmask[col]:
                bicluster_indices = []
                for i, (_, cells) in enumerate(biclusters):
                    # Check if the current cell is in any bicluster
                    for r in cells["rows"]:
                        for c in cells["cols"]:
                            if row == r and col == c:
                                bicluster_indices.append(i)

                new_row = sum(rmask[0:row])
                new_col = sum(cmask[0:col])

                if not bicluster_indices:
                    # If the cell is not in any bicluster, add a white circle
                    if not all(filtered_image[new_row, new_col, :] == [1., 1., 1., 1.]):
                        circle = Circle((new_col, new_row), radius, color='white', ec=None)
                        ax.add_patch(circle)
                else:
                    # Create a divided circle for each bicluster
                    num_parts = len(bicluster_indices)
                    part_angle = 360 / num_parts
                    for i, index in enumerate(bicluster_indices):
                        start_angle = i * part_angle
                        clr = colors[index]
                        wedge = Wedge((new_col, new_row), radius, start_angle, start_angle + part_angle, color=clr, ec=None)
                        ax.add_patch(wedge)


def main(
        var_translated_file: str, 
        gold_standard_translated_file: str, 
        metrics_zip_file: str, 
        metric: str, 
        representation: str, 
        plot_type: str, 
        accuracy_scores_file: str, 
        fun_file: str, 
        output_folder: str
    ) -> None:
    """
    Generate plots for MOEBA-HeCliDa results in comparison with gold standard.

    Args:
        var_translated_file (str): File with translated solutions.
        gold_standard_translated_file (str): File with translated gold standard solutions.
        metrics_zip_file (str): Zip file with metrics.
        metric (str): Metric used for evaluation.
        representation (str): Representation type.
        plot_type (str): Type of plot.
        output_folder (str): Output folder.
    """

    # Crear carpeta de salida si no existe
    Path(output_folder).mkdir(parents=True, exist_ok=True)

    # Cargar los archivos CSV
    result = pd.read_csv(var_translated_file, header=None)
    gold_standard = pd.read_csv(gold_standard_translated_file, header=None)

    # Convertir biclústeres a diccionarios de filas y columnas
    gold_biclusters = parse_bicluster(gold_standard.iloc[0], 'gs-')
    result_biclusters = [parse_bicluster(result.iloc[i], 'r-') for i in range(len(result))]

    # Si la representación es individual se juntan todas las soluciones en una sola
    if representation == 'individual':
        result_biclusters = [{f'r-{i}': list(d.values())[0] for i, d in enumerate(result_biclusters)}]

    ## 1. Grafo
    if plot_type == 'graph':

        for i in range(len(result_biclusters)):

            # Extraer la matriz de similaridad
            archive = zipfile.ZipFile(metrics_zip_file, 'r')
            content = archive.read(f"{metric}/solution-{i}.csv").decode('utf-8').splitlines()
            matrix = csv_to_nested_dict(content)

            # Crear la carpeta de salida si no existe
            Path(f"{output_folder}/graphs").mkdir(parents=True, exist_ok=True)

            # Crear el grafo
            G = nx.Graph()

            # Añadir nodos del gold standard en verde
            for bic in gold_biclusters.keys():
                G.add_node(bic, color='mediumseagreen')

            # Añadir nodos del resultado en morado
            for bic in result_biclusters[i].keys():
                G.add_node(bic, color='mediumpurple')

            # Añadir aristas
            for gsbic in gold_biclusters.keys():
                for rbic in result_biclusters[i].keys():
                    if matrix[gsbic][rbic] > 0:
                        G.add_edge(gsbic, rbic, weight=matrix[gsbic][rbic], color="gray")

            # Crear una visualización con pyvis
            net = Network(notebook=False, directed=False, height='100vh', width='100vw')

            # Activar la física para mejor distribución de nodos
            net.force_atlas_2based(gravity=-80, central_gravity=0.01, spring_length=200, spring_strength=0.01, damping=0.4)

            # Añadir nodos y establecer posiciones manualmente
            for node, data in G.nodes(data=True):
                net.add_node(node, label=node, color=data['color'], physics=True)

            # Añadir aristas a la visualización de pyvis
            for source, target, data in G.edges(data=True):
                net.add_edge(source, target, value=data['weight'], color=data['color'])

            # Generar el archivo HTML
            net.save_graph(f'{output_folder}/graphs/solution_{i}.html')

    ## 2. Imagen
    elif plot_type == 'picture':

        # Calcular tamaño de la matriz
        n = 0
        m = 0
        for gsbic, gscells in gold_biclusters.items():
            if max(gscells["rows"]) >= n:
                n = max(gscells["rows"]) + 1
            if max(gscells["cols"]) >= m:
                m = max(gscells["cols"]) + 1

        for i in range(len(result_biclusters)):

            # Extraer la matriz de similaridad
            archive = zipfile.ZipFile(metrics_zip_file, 'r')
            content = archive.read(f"{metric}/solution-{i}.csv").decode('utf-8').splitlines()
            matrix = csv_to_nested_dict(content)

            # Crear carpetas para las imagenes
            Path(f"{output_folder}/pictures/solution_{i}").mkdir(parents=True, exist_ok=True)

            # Generar colores para los biclusters
            colors = generate_colors(3, viridis)

            for j, (gsbic, gscells) in enumerate(gold_biclusters.items()):
                # Generar imagen completa
                gold_standard_image = create_image_matrix(n, m, gscells, colors[0])

                # Obtener las mejores biclusters
                best_biclusters = sorted(result_biclusters[i].items(), key=lambda x: matrix[gsbic][x[0]], reverse=True)[0:2]

                # Crear conjuntos para las filas y columnas relevantes
                relevant_rows = set(gscells["rows"] + best_biclusters[0][1]["rows"] + best_biclusters[1][1]["rows"])
                relevant_cols = set(gscells["cols"] + best_biclusters[0][1]["cols"] + best_biclusters[1][1]["cols"])

                # Crear máscaras para las filas y columnas relevantes
                row_mask = np.array([i in relevant_rows for i in range(n)])
                col_mask = np.array([i in relevant_cols for i in range(m)])

                # Filtrar la matriz
                filtered_image = gold_standard_image[row_mask][:, col_mask]

                # Generar imagen filtrada
                _, ax = plt.subplots(figsize=(10, 10))
                ax.imshow(filtered_image)

                # Añadir círculos de los tres biclusters resultado más cercano
                add_circles_to_plot(ax, n, m, best_biclusters, colors[1:], filtered_image, row_mask, col_mask)
                ax.set_title(f'B-GS-{j} vs top 2 in R-{i}')
                ax.axis('off')
                plt.savefig(f'{output_folder}/pictures/solution_{i}/gs_{j}.pdf')

    else:
        raise ValueError(f"Invalid plot type: {plot_type}")
    
    # Plot evaluated parallel coordinates
    if representation == "generic":
        df1 = pd.read_csv(fun_file)
        df2 = pd.read_csv(accuracy_scores_file)
        df = pd.concat([df1, df2], axis=1)
        fig = px.parallel_coordinates(
            df,
            color=metric,
            dimensions=df.columns,
            color_continuous_scale=px.colors.sequential.Blues,
            title="Evaluated plot of parallel coordinates",
        )
        fig.write_html(f"{output_folder}/evaluated_parallel_coordinates.html")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Plot MOEBA-HeCliDa results in comparison with gold standard."
    )
    parser.add_argument(
        "--var-translated-file",
        type=str,
        help="File with translated solutions.",
        required=True,
    )
    parser.add_argument(
        "--gold-standard-translated-file",
        type=str,
        help="File with translated gold standard.",
        required=True,
    )
    parser.add_argument(
        "--metric-zip-file",
        type=str,
        help="Zip file with metrics csvs.",
        required=True,
    )
    parser.add_argument(
        "--metric",
        type=str,
        help="Metric to calculate as similarity. It must be in the zip file as a folder with the same name.",
        required=True,
    )
    parser.add_argument(
        "--accuracy-scores-file",
        type=str,
        help="File with accuracy scores. Only required if representation is 'generic'.",
    )
    parser.add_argument(
        "--fun-file",
        type=str,
        help="File with function values. Only required if representation is 'generic'.",
    )
    parser.add_argument(
        "--representation", 
        choices=['individual', 'generic'], 
        default='generic', 
        help="Type of plot to display.",
    )
    parser.add_argument(
        "--plot-type", 
        choices=['graph', 'picture'], 
        default='graph', 
        help="Type of plot to display.",
    )
    parser.add_argument(
        "--output-folder",
        type=str,
        help="Output folder.",
        default="output",
    )
    args = parser.parse_args()

    if args.representation == "generic" and args.accuracy_scores_file is None:
        raise ValueError("File with accuracy scores is required if representation is 'generic'.")

    if args.representation == "generic" and args.fun_file is None:
        raise ValueError("File with function values is required if representation is 'generic'.")

    main(args.var_translated_file, args.gold_standard_translated_file, args.metric_zip_file, args.metric, args.representation, args.plot_type, args.accuracy_scores_file, args.fun_file, args.output_folder)
