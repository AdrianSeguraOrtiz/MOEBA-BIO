import random
import pandas as pd
import json
import argparse

from sklearn.preprocessing import MinMaxScaler

"""
This script processes a dataset and its associated bicluster information genetated by gbic.
It reads a JSON file containing specifications of biclusters and a TSV dataset file.
Rows in the dataset that are not part of any bicluster are replaced with random rows from biclusters.
Finally, the script outputs several files:
1. A CSV file summarizing the biclusters.
2. A CSV file with the modified dataset.
3. A JSON file detailing the data types of the dataset columns.

Usage:
    python script.py --biclusters-file <path_to_biclusters_json> --data-file <path_to_data_tsv>
"""

# Command-line arguments parsing
parser = argparse.ArgumentParser(description='Process bicluster and data files with explicit parameter names.')
parser.add_argument("--biclusters-file", required=True, help="JSON file containing the biclusters' specification")
parser.add_argument("--data-file", required=True, help="TSV file containing the dataset")
args = parser.parse_args()

# Load biclusters specification from JSON
with open(args.biclusters_file, 'r') as file:
    biclusters_data = json.load(file)

# Extracting biclusters
biclusters = biclusters_data['biclusters']
biclusters_list = [[bic["X"], bic["Y"]] for bic in biclusters.values()]

# Load dataset
df = pd.read_csv(args.data_file, sep='\t', decimal=',')

# Remove the first column
df.drop(columns=df.columns[0], inplace=True)

# Replace ungrouped rows with random copies of grouped rows
for row_index in range(len(df)):
    is_grouped = any(row_index in bic[0] for bic in biclusters_list)
    if not is_grouped:
        chosen_bic = random.choice(biclusters_list)
        random_row_index = random.choice(chosen_bic[0])
        df.iloc[row_index] = df.iloc[random_row_index]
        chosen_bic[0].append(row_index)

# Save biclusters to CSV
output_base = args.data_file.split(".")[0]
bic_clusters_filename = f'{output_base}-translated.csv'
with open(bic_clusters_filename, "w") as file:
    bic_clusters_info = ", ".join(
        f"Bicluster{i}: (rows: [{' '.join(map(str, bic[0]))}] cols: [{' '.join(map(str, bic[1]))}])"
        for i, bic in enumerate(biclusters_list)
    )
    file.write(bic_clusters_info)

# Normalize the dataset
scaler = MinMaxScaler()
for column in df.columns:
    if pd.api.types.is_numeric_dtype(df[column]):
        df[column] = scaler.fit_transform(df[column].values.reshape(-1, 1))

# Save the modified dataset to CSV
df.to_csv(f'{output_base}-data.csv', index=False)

# Create a dictionary with column names and their data types
column_data_types = {col: str(dtype) for col, dtype in df.dtypes.items()}

# Save the data types dictionary as JSON
types_json_path = f'{output_base}-types.json'
with open(types_json_path, "w") as file:
    json.dump(column_data_types, file, indent=4, ensure_ascii=False)
