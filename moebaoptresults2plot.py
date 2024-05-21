import argparse
import math
from pathlib import Path

import numpy as np
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
import seaborn as sns
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
from plotly.subplots import make_subplots


def read_fitness_evolution_file(fitness_evolution_file: str) -> list[str]:
    """
    Read the fitness evolution file and return a list of lines.

    Args:
    - fitness_evolution_file (str): The path to the fitness evolution file.

    Returns:
    - List[str]: A list of lines read from the file.
    """
    # Open the file with the fitness values
    with open(fitness_evolution_file, "r") as f:
        # Each line contains the evolution of a different objective
        lines = f.readlines()

    return lines


def read_fun_file(fun_file: str, objectives: list) -> pd.DataFrame:
    """
    Read a CSV file into a DataFrame and reorganize columns based on a list of objectives.

    Args:
        fun_file (str): The path to the CSV file.
        objectives (list): The list of column names in the desired order.

    Returns:
        pd.DataFrame: The DataFrame with columns reorganized based on the objectives list.
    """
    # Read the CSV file into a DataFrame
    df = pd.read_csv(fun_file)

    # Reorganize columns to match the order of objectives list
    df = df[objectives]

    # Convert DataFrame values to float
    df = df.astype(float)

    return df


def plot_fitness_evolution(
    fitness_evolution_lines: list[str], objectives: list[str], output_file: str
):
    """
    Plots the fitness evolution for each objective.

    Args:
        fitness_evolution_lines (List[str]): A list of strings representing the fitness evolution for each objective.
        objectives (List[str]): A list of strings representing the objectives.
        output_file (str): The file name to save the plot as HTML.

    Returns:
        None
    """
    # Create grid for graphs
    if len(fitness_evolution_lines) == 1:
        r, c = 1, 1
    elif len(fitness_evolution_lines) == 2:
        r, c = 1, 2
    else:
        r = math.ceil(len(fitness_evolution_lines) / 2)
        c = 2

    # Make plots
    fig = make_subplots(rows=r, cols=c, subplot_titles=objectives)

    # For each objective ...
    for i in range(len(fitness_evolution_lines)):

        # Read the evolution of its values
        str_fitness = fitness_evolution_lines[i].split(", ")

        # Convert it to the appropriate type (float)
        fitness = [float(v) for v in str_fitness]

        # Get row and column index
        curr_row = math.ceil((i + 1) / c)
        curr_col = (i + 1) - (c * (curr_row - 1))

        # Plot it under the label of its function
        fig.add_trace(
            go.Scatter(x=list(range(len(fitness))), y=fitness),
            row=curr_row,
            col=curr_col,
        )
        fig.update_xaxes(title_text="Generation", row=curr_row, col=curr_col)
        fig.update_yaxes(title_text="Fitness", row=curr_row, col=curr_col)

    # Customize and save the figure
    fig.update_layout(title_text="Fitness evolution", showlegend=False)
    fig.write_html(output_file)


def plot_parallel_coordinates(fun_df: pd.DataFrame, objectives: list, output_file: str):
    """
    Plot parallel coordinates graph using the given DataFrame and objectives,
    and save the output to the specified file.

    Parameters:
    - fun_df (pandas.DataFrame): The DataFrame containing the data to plot.
    - objectives (list): The list of column names to use as dimensions for the parallel coordinates.
    - output_file (str): The file path to save the output graph.
    """

    # Create the parallel coordinates figure
    fig = px.parallel_coordinates(
        fun_df, dimensions=objectives, title="Graph of parallel coordinates"
    )

    # Save the figure as HTML
    fig.write_html(output_file)


def plot_2D_pareto_front(fun_df: pd.DataFrame, objectives: list, output_file: str):
    """
    Plots a 2D Pareto front from a DataFrame of fitness values.

    Args:
        fun_df (pd.DataFrame): DataFrame containing fitness values.
        objectives (list): List of objective names.
        output_file (str): Path to save the plot as an HTML file.
    """

    # Get columns as lists
    fitness_o1 = fun_df[objectives[0]].tolist()
    fitness_o2 = fun_df[objectives[1]].tolist()

    # Obtain the order corresponding to the first objective in order to plot the front in an appropriate way.
    sorted_idx = np.argsort(fitness_o1)

    # Sort all vectors according to the indices obtained above.
    fitness_o1 = [fitness_o1[i] for i in sorted_idx]
    fitness_o2 = [fitness_o2[i] for i in sorted_idx]

    # Plot, customize, save the figure
    fig = px.line(x=fitness_o1, y=fitness_o2, markers=True, title="Pareto front")
    fig.update_xaxes(title_text=objectives[0])
    fig.update_yaxes(title_text=objectives[1])
    fig.write_html(output_file)


def plot_3D_pareto_front(fun_df: pd.DataFrame, objectives: list, output_file: str):
    """
    Plots a 3D scatter plot of the pareto front.

    Args:
        fun_df (pd.DataFrame): The data frame containing the objective values.
        objectives (list): The names of the objective columns in the data frame.
        output_file (str): The name of the output HTML file.

    Returns:
        None
    """
    # Create the 3D scatter plot
    fig = go.Figure(
        data=[
            go.Scatter3d(
                x=fun_df[objectives[0]],
                y=fun_df[objectives[1]],
                z=fun_df[objectives[2]],
                mode="markers",
            )
        ]
    )
    fig.update_traces(marker_size = 4)

    # Set the axis names and the title of the plot
    fig.update_layout(
        scene=dict(
            xaxis_title=objectives[0],
            yaxis_title=objectives[1],
            zaxis_title=objectives[2],
            xaxis_title_font=dict(size=20),
            yaxis_title_font=dict(size=20),
            zaxis_title_font=dict(size=20),
            xaxis=dict(tickfont=dict(size=14)),
            yaxis=dict(tickfont=dict(size=14)),
            zaxis=dict(tickfont=dict(size=14)),
        ),
        title="Pareto front",
    )

    # Save the plot to an HTML file
    fig.write_html(output_file)


def read_bicluster_count_file(bicluster_count_file: str, population_size: int) -> pd.DataFrame:
    """
    Reads the CSV file containing the bicluster counts per generation and expands the data to a single row
    per bicluster count. The expanded data frame contains the number of biclusters and the generation
    they belong to.

    Parameters:
    - bicluster_count_file (str): The path to the CSV file containing the bicluster counts.
    - population_size (int): The size of the population used to generate the bicluster counts.

    Returns:
        A Pandas DataFrame with the expanded bicluster counts.
    """
    # Read the CSV file
    data = pd.read_csv(bicluster_count_file, header=None, delimiter=', ', engine='python')

    # Expand the data by repeating each row `count` times, where `count` is the number of
    # biclusters for that generation multiplied by the population size and divided by 100.
    expanded_data = []
    generations = []
    for _, row in data.iterrows():
        for gen in data.columns[1:]:
            count = int(row[gen] * population_size / 100)
            expanded_data.extend([row[0]] * count)
            generations.extend([gen] * count)

    # Create a new DataFrame with the expanded data
    df = pd.DataFrame({'Number of Biclusters': expanded_data, 'Generation': generations})

    return df

def plot_bicluster_count(df: pd.DataFrame,
                         plot_type: str,
                         output_file: str) -> None:
    """
    Plots the distribution of biclusters per generation using the specified plot type.
    The x-axis represents the generation number, and the y-axis represents the number of biclusters.
    The output is saved as an image file.

    Parameters:
    - df (pandas.DataFrame): The data frame containing the bicluster counts per generation.
    - plot_type (str): The type of plot to generate (e.g., 'violin', 'box', 'histogram', 'density', 'ecdf').
    - output_file (str): The path to the output file where the plot will be saved.
    """
    # Set figure size
    plt.figure(figsize=(45, 15))

    # Plot the data
    if plot_type == 'violin':
        # Plot violin plot
        sns.violinplot(x='Generation', y='Number of Biclusters', data=df)
    elif plot_type == 'box':
        # Plot box plot
        ax = sns.boxplot(x='Generation', y='Number of Biclusters', data=df)
        ax.xaxis.set_major_locator(ticker.MultipleLocator(25))
        ax.xaxis.set_major_formatter(ticker.ScalarFormatter())
    elif plot_type == 'histogram':
        # Plot histogram
        # Convert 'Generation' to categorical for better histogram plotting
        df['Generation'] = pd.Categorical(df['Generation'],
                                          categories=df['Generation'].unique(),
                                          ordered=True)
        sns.histplot(data=df,
                     x='Number of Biclusters',
                     hue='Generation',
                     element='step',
                     kde=False,
                     stat="count")
    elif plot_type == 'density':
        # Plot density plot
        sns.kdeplot(data=df, x='Number of Biclusters', hue='Generation', common_norm=False)
    elif plot_type == 'ecdf':
        # Plot ECDF plot
        sns.ecdfplot(data=df, x='Number of Biclusters', hue='Generation')

    # Set plot title
    plt.title('Distribution of Biclusters per Generation')

    # Save the plot to an image file
    plt.savefig(output_file)

def main(fun_file: str, fitness_evolution_file: str, bicluster_count_file: str, population_size: int, bicluster_count_plot_type: str, output_folder: str):
    """
    Refactored function to generate fitness evolution plots and add them to a zip file.
    If there are multiple objectives, it also generates parallel coordinates plots and adds them to the zip file.
    If there are two objectives, it also generates 2D pareto front plots and adds them to the zip file.
    If there are three objectives, it also generates 3D pareto front plots and adds them to the zip file.

    Args:
        fun_file (str): The path to the fun file.
        fitness_evolution_file (str): The path to the fitness evolution file.
        bicluster_count_file (str): The path to the bicluster count file.
        population_size (int): The population size.
        bicluster_count_plot_type (str): The type of plot to generate for the bicluster count.
        output_folder (str): The path to the output folder where the zip file will be created.
    """

    # Create the output folder if it doesn't exist with Pathlib
    Path(output_folder).mkdir(parents=True, exist_ok=True)

    # Get objectives names from fun_file
    objectives = pd.read_csv(fun_file, nrows=0).columns.tolist()

    # 1. Parallel Coordinates (if there is more than one objective)
    if len(objectives) > 1:
        ## Read fun file
        df = read_fun_file(fun_file, objectives)

        ## Set parallel coordinates filename
        filename = f"{output_folder}/parallel_coordinates.html"

        ## Plot parallel coordinates
        plot_parallel_coordinates(df, objectives, filename)

    # 2. Pareto Front 2D (if there are two objectives)
    if len(objectives) == 2:
        ## Set pareto front filename
        filename = f"{output_folder}/pareto_front_2D.html"

        ## Plot pareto front
        plot_2D_pareto_front(df, objectives, filename)

    # 3. Pareto Front 3D (if there are three objectives)
    if len(objectives) == 3:
        ## Set pareto front filename
        filename = f"{output_folder}/pareto_front_3D.html"

        ## Plot pareto front
        plot_3D_pareto_front(df, objectives, filename)

        ## Plot 2D pareto front for each pair of objectives
        for i in range(len(objectives)):
            for j in range(i + 1, len(objectives)):
                ## Set pareto front filename
                filename = f"{output_folder}/pareto_front_2D_{objectives[i]}-{objectives[j]}.html"

                ## Plot pareto front
                plot_2D_pareto_front(df, [objectives[i], objectives[j]], filename)
    
    # 4. Fitness evolution
    if fitness_evolution_file is not None:
        ## Set fitness evolution filename
        filename = f"{output_folder}/fitness_evolution.html"

        ## Read fitness evolution
        lines = read_fitness_evolution_file(fitness_evolution_file)

        ## Plot fitness evolution
        plot_fitness_evolution(lines, objectives, filename)

    # 5. Bicluster count evolution
    if bicluster_count_file is not None:
        if population_size is None:
            raise ValueError("Population size must be provided if bicluster count file is provided.")

        ## Set bicluster count filename
        filename = f"{output_folder}/bicluster_count.pdf"

        ## Read bicluster count file
        df = read_bicluster_count_file(bicluster_count_file, population_size)

        ## Plot bicluster count
        plot_bicluster_count(df, bicluster_count_plot_type, filename)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Plot execution info generated by NOEBA-HeCliDa Optimization Algorithm."
    )
    parser.add_argument(
        "--fun-file",
        type=str,
        help="File with function values.",
        required=True,
    )
    parser.add_argument(
        "--fitness-evolution-file",
        type=str,
        help="Fitness evolution file.",
    )
    parser.add_argument(
        "--bicluster-count-file",
        type=str,
        help="Bicluster count file.",
    )
    parser.add_argument(
        "--population-size", 
        type=int, 
        help="Multiplier for bicluster counts.",
    )
    parser.add_argument(
        "--bicluster-count-plot-type", 
        choices=['violin', 'box', 'histogram', 'density', 'ecdf'], 
        default='violin', 
        help="Type of plot to display.",
    )
    parser.add_argument(
        "--output-folder",
        type=str,
        help="Output folder.",
        default="results",
    )
    args = parser.parse_args()

    main(args.fun_file, args.fitness_evolution_file, args.bicluster_count_file, args.population_size, args.bicluster_count_plot_type, args.output_folder)
