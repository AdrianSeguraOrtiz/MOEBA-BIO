import random
import pandas as pd
import json
import argparse

from sklearn.preprocessing import MinMaxScaler

"""
This script processes a dataset and its associated bicluster information genetated by gbic.
It reads a JSON file containing specifications of biclusters and a TSV dataset file.
Overlapped rows are distributed among the biclusters and ungrouped rows are replaced by copies of grouped rows.
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
df = pd.read_csv(args.data_file, sep='\t', decimal='.')

# Remove the first column
df.drop(columns=df.columns[0], inplace=True)

# Check the status of each row
status = []
for row_index in range(len(df)):
    status.append(sum(row_index in bic[0] for bic in biclusters_list))

# Duplicate overlapped rows and delete one of ungrouped rows
for row_index in range(len(df)):
    if status[row_index] > 1:
        # Get the indexes of the biclusters where the row is part of
        bic_indexes = [i for i, bic in enumerate(biclusters_list) if row_index in bic[0]]

        # For each one less the last one
        for i in range(len(bic_indexes) - 1):
            # Get the bicluster
            bic = biclusters_list[bic_indexes[i]]

            # Select a random row from the same bicluster
            same_bic_copy = random.choice([row_index for row_index in bic[0] if status[row_index] == 1])

            # Select a random ungrouped row
            ungrouped_rows = [row_index for row_index in range(len(status)) if status[row_index] == 0]
            ungrouped_replace = random.choice(ungrouped_rows) if len(ungrouped_rows) > 0 else random.choice([row_index for row_index in biclusters_list[bic_indexes[i+1]][0] if status[row_index] == 1])

            # In the ungrouped row we copy the data of the row in the same bilcluster but changing values of the columns of the bicluster by the data of the overlapped row 
            # In the overlapped row and in the columns of the bicluster we copy the data of the ungrouped row
            ungrouped_replace_data = df.iloc[ungrouped_replace]
            df.iloc[ungrouped_replace] = df.iloc[same_bic_copy]
            for col in bic[1]:
                df.iloc[ungrouped_replace, col] = df.iloc[row_index, col]
                df.iloc[row_index, col] = ungrouped_replace_data.iloc[col]
            
            # Ungrouped row is grouped now in the bicluster
            status[ungrouped_replace] = 1
            for j in range(len(bic[0])):
                if bic[0][j] == row_index:
                    bic[0][j] = ungrouped_replace
                
        # At the end overlapped row is grouped
        status[row_index] = 1

# The rows that remain ungrouped are replaced by copies of grouped rows
for row_index in range(len(df)):
    if status[row_index] == 0:
        chosen_bic = random.choice(biclusters_list)
        random_row_index = random.choice(chosen_bic[0])
        df.iloc[row_index] = df.iloc[random_row_index]
        chosen_bic[0].append(row_index)
        status[row_index] = 1

# Check the status of each row
ungrouped = 0
grouped = 0
overlapped = 0
num_rows = biclusters_data['#DatasetRows']
for row_index in range(int(num_rows)):
    value = sum(row_index in bic[0] for bic in biclusters_list)
    if value == 0:
        ungrouped += 1
    elif value == 1:
        grouped += 1
    elif value > 1:
        overlapped += 1

# Printing statistics
print(f"Grouped: {grouped/num_rows*100}%, Ungrouped: {ungrouped/num_rows*100}%, Overlapped: {overlapped/num_rows*100}%")

# Save biclusters to CSV
output_base = args.data_file.split(".")[0]
bic_clusters_filename = f'{output_base}-translated.csv'
biclusters_list_sorted = [[sorted(subsublist) for subsublist in sublist] for sublist in biclusters_list]
biclusters_list_sorted.sort(key=lambda x: min(x[0]))
with open(bic_clusters_filename, "w") as file:
    bic_clusters_info = ", ".join(
        f"Bicluster{i}: (rows: [{' '.join(map(str, bic[0]))}] cols: [{' '.join(map(str, bic[1]))}])"
        for i, bic in enumerate(biclusters_list_sorted)
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
