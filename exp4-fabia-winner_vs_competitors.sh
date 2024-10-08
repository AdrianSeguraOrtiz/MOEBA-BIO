#!/bin/bash

# Define the root output folder for the experiments
root_output_folder=exp4-competitors
# Create the output folder if it doesn't exist
mkdir -p $root_output_folder

# Retrieve values from exp3 for comparison
for data_file in fabia_simulated_data/*-data.csv; do
    # Remove the "-data.csv" suffix to get the instance path
    instance_path=${data_file%-data.csv}
    # Extract the base name of the instance path (file identifier)
    id=$(basename $instance_path)
    # Define the validation folder path
    val_folder=$root_output_folder/$id/validation
    # Create the validation folder
    mkdir -p $val_folder
    
    # Extract the relevant data from exp3 for comparison
    exp3_file="exp3-fabia-winner_vs_candidates/$id/config_comparison.csv"
    # Copy the header and winner row to a new file for MOEBA-BIO
    head -n 1 $exp3_file > $val_folder/MOEBA-BIO-scores.csv
    awk -F',' '/^winner/ {print $0}' "$exp3_file" >> $val_folder/MOEBA-BIO-scores.csv
    # Remove the first column from the scores file (configuration name)
    cut -d',' -f2- $val_folder/MOEBA-BIO-scores.csv > temp.csv && mv temp.csv $val_folder/MOEBA-BIO-scores.csv
done

# Function to run competitor algorithms and perform validation
run_competitors() {
    instance_path=$1
    algorithm=$2
    output_folder=$3
    # Define the output file for the algorithm's inferred biclusters
    output_file=$output_folder/$algorithm-bics-translated.csv

    # Run the competitor algorithm on the input data file
    python competitors.py --algorithm $algorithm --input-file $instance_path-data.csv --output-file $output_file

    # Get total memory and allocate 95% for Java execution
    total_mem=$(free --kilo | awk '/^Mem:/ {print $2}')
    xmx=$((total_mem * 95 / 100))
    
    # Run Java validation for the competitor algorithm using several validation metrics
    java -Xmx${xmx}k -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.validation.ValidationRunner \
        --inferred-translated $output_file \
        --gold-standard-translated $instance_path-translated.csv \
        --validation-metrics "ScorePrelicRelevance;ScorePrelicRecovery;ScoreLiuWang;ScoreDice;ScoreAyadi;ScoreErenRelevance;ScoreErenRecovery;ClusteringErrorComplementary" \
        --representation GENERIC \
        --output-file $output_folder/validation/$algorithm-scores.csv \
        --num-threads 32
}

# Export the function for use in parallel processing
export -f run_competitors

# Activate the virtual environment
source .venv/bin/activate
# List of competitor algorithms
algorithms=('xMotifs' 'OPSM' 'Plaid' 'CCA' 'Bimax' 'ISA' 'BiBit' 'LAS' 'Spectral')

# Loop through all data files and run each competitor algorithm
for data_file in fabia_simulated_data/*-data.csv; do
    # Extract the problem name from the data file
    problema=${data_file%-data.csv}
    # Run each algorithm on the current data file
    for algorithm in ${algorithms[@]}; do
        run_competitors $problema $algorithm $root_output_folder/$id
    done
done

# Deactivate the virtual environment
deactivate

# Process validation results and concatenate scores for each instance
for data_file in fabia_simulated_data/*-data.csv; do
    # Extract the instance path and identifier
    instance_path=${data_file%-data.csv}
    id=$(basename $instance_path)
    val_folder=$root_output_folder/$id/validation

    # Define the output file for concatenated scores
    output_file=$val_folder/concatenated_scores.csv

    # Variable to track if the header has been written to the output file
    header_written=false

    # Loop through all *-scores.csv files in the validation folder
    for file in $val_folder/*-scores.csv; do
        # Extract the file identifier (part before -scores.csv)
        file_id=$(basename "$file" -scores.csv)

        # Write the header if it hasn't been written yet
        if [ "$header_written" = false ]; then
            # Write the header with an additional 'FileID' column
            echo "FileID,$(head -n 1 "$file")" > "$output_file"
            header_written=true
        fi

        # If processing the MOEBA-BIO file, calculate max and median values
        if [ "$file_id" = "MOEBA-BIO" ]; then
            # Inline Python script to calculate max and median
            python3 - <<EOF >> "$output_file"
import pandas as pd
df = pd.read_csv("$file")
max_values = df.max()
max_values_row = "MOEBA-BIO-best," + ",".join(map(str, max_values.values[::2]))
median_values = df.median()
median_values_row = "MOEBA-BIO-median," + ",".join(map(str, median_values.values[1::2]))
print(max_values_row)
print(median_values_row)
EOF
        else
            # Append the content of the file with the file_id as the first column
            tail -n +2 "$file" | awk -v id="$file_id" '{print id "," $0}' >> "$output_file"
        fi
    done
done

# Define the output directory for column-based ranking files
output_dir=$root_output_folder/ranking
# Create the output directory if it doesn't exist
mkdir -p $output_dir

# Track if processing the first file
first_file=true
# Initialize the header with "Comparison"
first_line="Comparison"

# Loop through each concatenated_scores.csv file for each instance
for comparison_file in $root_output_folder/*/validation/concatenated_scores.csv; do

    # Extract the dataset ID from the directory structure
    id=$(basename $(dirname $(dirname $comparison_file)))
    echo "Processing $id"

    # Read the header to get the column names
    header=$(head -n 1 $comparison_file)
    
    # Convert the header into an array of column names
    IFS=',' read -r -a column_names <<< "$header"

    # Process each row in the comparison file and extract the configuration name
    if [[ $first_file == true ]]; then
        while IFS=',' read -r -a line; do
            config_name="${line[0]}" 
            first_line="${first_line},${config_name}"
        done < <(tail -n +2 $comparison_file)
    fi

    # Loop through each row and store the values in the respective column files
    first_time=true
    while IFS=',' read -r -a line; do
        config_name="${line[0]}" 
        for i in "${!line[@]}"; do
            if [ "$i" -gt 0 ]; then 
                column_name="${column_names[$i]}"

                # Write the header if processing the first file
                if [[ $first_file == true ]]; then
                    echo -n "$first_line" > "$output_dir/${column_name}.csv"
                fi
                # Write the values for each column in the respective files
                if [[ $first_time == true ]]; then
                    echo >> "$output_dir/${column_name}.csv"
                    echo -n "$id,${line[$i]}" >> "$output_dir/${column_name}.csv"
                else 
                    echo -n ",${line[$i]}" >> "$output_dir/${column_name}.csv"
                fi
            fi
        done
        first_time=false
        first_file=false
    done < <(tail -n +2 $comparison_file) # Skip the header line

done

# Change to the controlTest directory
cd control-test
# Run the Friedman test for each CSV file in the ranking directory
for file in ../$output_dir/*.csv; do
    java Friedman $file > ${file%.csv}.txt
done
cd ..
