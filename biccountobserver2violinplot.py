import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import argparse

"""
Script to plot the distribution of biclusters per generation from CSV data.
Each row in the CSV should represent a bicluster count, with the first column as labels and the rest as generations.

Usage:
    python script.py --file-path <path_to_csv_file> --population-size <multiplier> --plot-type <violin|box>
"""

# Parse command-line arguments
parser = argparse.ArgumentParser(description='Plot bicluster distributions using a Seaborn plot.')
parser.add_argument("--file-path", required=True, help="CSV file path with bicluster data.")
parser.add_argument("--population-size", type=int, required=True, help="Multiplier for bicluster counts.")
parser.add_argument("--plot-type", choices=['violin', 'box', 'histogram', 'density', 'ecdf'], default='violin', help="Type of plot to display (violin or box).")
args = parser.parse_args()

# Load and process data
data = pd.read_csv(args.file_path, header=None, delimiter=', ', engine='python')
expanded_data = []
generations = []
for i, row in data.iterrows():
    for gen in data.columns[1:]:
        count = int(row[gen] * args.population_size / 100)
        expanded_data.extend([row[0]] * count)
        generations.extend([gen] * count)
df = pd.DataFrame({'Number of Biclusters': expanded_data, 'Generation': generations})

# Plot data
plt.figure(figsize=(18, 10))

if args.plot_type == 'violin':
    sns.violinplot(x='Generation', y='Number of Biclusters', data=df)
elif args.plot_type == 'box':
    sns.boxplot(x='Generation', y='Number of Biclusters', data=df)
elif args.plot_type == 'histogram':
    # Convert 'Generation' to categorical for better histogram plotting
    df['Generation'] = pd.Categorical(df['Generation'], categories=df['Generation'].unique(), ordered=True)
    sns.histplot(data=df, x='Number of Biclusters', hue='Generation', element='step', kde=False, stat="count")
elif args.plot_type == 'density':
    sns.kdeplot(data=df, x='Number of Biclusters', hue='Generation', common_norm=False)
elif args.plot_type == 'ecdf':
    sns.ecdfplot(data=df, x='Number of Biclusters', hue='Generation')

plt.title('Distribution of Biclusters per Generation')
plt.show()
