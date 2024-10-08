#!/bin/bash
# Get total memory in kilobytes
total_mem=$(free --kilo | awk '/^Mem:/ {print $2}')
# Set 95% of the total memory for the Java process
xmx=$((total_mem * 95 / 100))

# Define the root output folder
root_output_folder=exp3-fabia-winner_vs_candidates
# Create the output folder if it doesn't exist
mkdir -p $root_output_folder

# Loop over all the CSV data files in the specified directory
for file in fabia_simulated_data/*-data.csv
do
    # Remove the "-data.csv" suffix to get the base path
    path=${file%-data.csv}
    # Extract the base name of the path (file identifier)
    id=$(basename $path)
    data_file=$file
    type_file=${path}-types.json

    # Create a subdirectory for the current file in the output folder
    mkdir -p ./$root_output_folder/$id/

    # Function to execute the Java program
    run_java() {
        out_folder=$1
        str_fitness_functions=$2
        algorithm=$3
        data_file=$4
        type_file=$5

        # Create the output directory for this execution
        mkdir -p $out_folder
        # Run the Java program with the specified parameters
        java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.Runner \
            --representation GENERIC \
            --input-dataset $data_file \
            --input-column-types $type_file \
            --str-fitness-functions "$str_fitness_functions" \
            --population-size 100 \
            --max-evaluations 150000 \
            --str-algorithm $algorithm \
            --mutation-probability 0.00028735760298394047 \
            --crossover-probability 0.6378843409897743 \
            --observers "BiclusterCountObserver;FitnessEvolutionMinObserver" \
            --crossover-operator "PartiallyMappedCrossover;BicUniformCrossover;CellUniformCrossover" \
            --mutation-operator "SwapMutation;BicUniformMutation;CellUniformMutation" \
            --summarise-individual-objectives Mean \
            --output-folder $out_folder
    }

    # Export the function for parallel processing
    export -f run_java

    # Define lists to store output folders, fitness functions, and algorithms
    out_folders=()
    fitness_functions=()
    algorithms=()

    # 1. Configuration for the "Winner"
    for i in {1..5}; do
        out_folders+=("./$root_output_folder/$id/winner/$i/")
        fitness_functions+=("BiclusterVarianceNorm;RowVarianceNormComp;BiclusterSizeNumBicsNormComp(coherenceWeight=0.08019762516427839,rowsWeight=0.8746381985928995);RegulatoryCoherenceNormComp")
        algorithms+=("IBEA-SingleThread")
    done

    # 2. Configuration for Candidate 1 (without Regulatory Coherence)
    for i in {1..5}; do
        out_folders+=("./$root_output_folder/$id/candidate-1-noRegCoherence/$i/")
        fitness_functions+=("BiclusterVarianceNorm;RowVarianceNormComp;BiclusterSizeNumBicsNormComp(coherenceWeight=0.08019762516427839,rowsWeight=0.8746381985928995)")
        algorithms+=("IBEA-SingleThread")
    done

    # 3. Configuration for Candidate 2 (without Row Variance)
    for i in {1..5}; do
        out_folders+=("./$root_output_folder/$id/candidate-2-noRowVariance/$i/")
        fitness_functions+=("BiclusterVarianceNorm;BiclusterSizeNumBicsNormComp(coherenceWeight=0.08019762516427839,rowsWeight=0.8746381985928995);RegulatoryCoherenceNormComp")
        algorithms+=("IBEA-SingleThread")
    done

    # 4. Configuration for Candidate 3 (with Distance Between Biclusters)
    for i in {1..5}; do
        out_folders+=("./$root_output_folder/$id/candidate-3-DistBetweenBics/$i/")
        fitness_functions+=("BiclusterVarianceNorm;BiclusterSizeNumBicsNormComp(coherenceWeight=0.08019762516427839,rowsWeight=0.8746381985928995);RegulatoryCoherenceNormComp;DistanceBetweenBiclustersNormComp")
        algorithms+=("IBEA-SingleThread")
    done

    # Run the Java programs in parallel for each configuration
    parallel --link run_java ::: "${out_folders[@]}" ::: "${fitness_functions[@]}" ::: "${algorithms[@]}" ::: "${data_file}" ::: "${type_file}"

    # Process results for each output folder
    for output_folder in ./$root_output_folder/$id/*/*
    do
        # Define the translated gold standard file path
        translated_gold_standard=$path-translated.csv
        conf=$(basename $(dirname $output_folder))
        i=$(basename $output_folder)
        confid=$conf-$i

        # Generate plots for the current execution
        python moebaoptresults2plot.py \
            --fun-file $output_folder/FUN.csv \
            --fitness-evolution-file $output_folder/FitnessEvolutionMinObserver.csv \
            --bicluster-count-file $output_folder/BiclusterCountObserver.csv \
            --population-size 100 \
            --bicluster-count-plot-type box \
            --output-folder $output_folder/plots

        ## Comparison with the gold standard
        ### Compute validation metrics
        java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.validation.ValidationRunner \
            --inferred-translated $output_folder/VAR-translated.csv \
            --gold-standard-translated $translated_gold_standard \
            --validation-metrics "ScorePrelicRelevance;ScorePrelicRecovery;ScoreLiuWang;ScoreDice;ScoreAyadi;ScoreErenRelevance;ScoreErenRecovery;ClusteringErrorComplementary" \
            --representation "GENERIC" \
            --output-file $output_folder/validation/scores.csv 

        # Define an associative array in bash to store the maximum and median values for each metric
        declare -A stats

        # Read the first row to get the column names
        IFS=',' read -r -a column_names < <(head -n 1 $output_folder/validation/scores.csv)

        # Loop through each column in the CSV file
        for col in $(seq 1 ${#column_names[@]})
        do
            # Get the column name
            col_name="${column_names[$((col-1))]}"
            
            # Extract the data from the column (excluding the header)
            column_data=$(awk -F, -v col=$col '{print $col}' $output_folder/validation/scores.csv | tail -n +2)
            
            # Compute the maximum value in the column
            max_value=$(echo "$column_data" | sort -gr | head -n 1)
            
            # Compute the median value in the column
            sorted_column=$(echo "$column_data" | sort -g)
            count=$(echo "$sorted_column" | wc -l)
            
            if [ $((count % 2)) -eq 1 ]; then
                # If the number of rows is odd, the median is the middle value
                mediana=$(echo "$sorted_column" | awk -v mid=$((count/2+1)) 'NR==mid')
            else
                # If the number of rows is even, the median is the average of the two middle values
                val1=$(echo "$sorted_column" | awk -v mid=$((count/2)) 'NR==mid')
                val2=$(echo "$sorted_column" | awk -v mid=$((count/2+1)) 'NR==mid')
                mediana=$(echo "scale=8; ($val1+$val2)/2" | bc)
            fi
            
            # Store the maximum and median values in the associative array
            stats["$col_name-max"]=$max_value
            stats["$col_name-mediana"]=$mediana
        done

        # Sort the keys alphabetically and store them in a list
        sorted_keys=($(for key in "${!stats[@]}"; do echo "$key"; done | sort))

        # Create strings with the keys and values in the same order
        sorted_keys_string=$(IFS=','; echo "${sorted_keys[*]}")
        sorted_values_string=$(IFS=','; for key in "${sorted_keys[@]}"; do echo -n "${stats[$key]},"; done | sed 's/,$//')

        # Write the results to the comparison CSV file
        if [ ! -f $root_output_folder/$id/config_comparison.csv ]; then
            echo "Configuration,$sorted_keys_string" > $root_output_folder/$id/config_comparison.csv
        fi
        echo "$confid,$sorted_values_string" >> $root_output_folder/$id/config_comparison.csv

    done

done