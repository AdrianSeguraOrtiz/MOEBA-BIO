import json
import argparse

"""
Usage:
    python script.py --biclusters-file <path_to_biclusters_json>
"""

# Command-line arguments parsing
parser = argparse.ArgumentParser(description='Get statistics from a JSON file containing specifications of biclusters.')
parser.add_argument("--biclusters-file", required=True, help="JSON file containing the biclusters' specification")
args = parser.parse_args()

# Load biclusters specification from JSON
with open(args.biclusters_file, 'r') as file:
    biclusters_data = json.load(file)

# Extracting biclusters
biclusters = biclusters_data['biclusters']
biclusters_list = [[bic["X"], bic["Y"]] for bic in biclusters.values()]

# Calculating statistics
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
