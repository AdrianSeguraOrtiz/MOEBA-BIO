total_mem=$(free --kilo | awk '/^Mem:/ {print $2}')
xmx=$((total_mem * 95 / 100))

population_size=500
num_evaluations=100000
root_output_folder=exp1-gbic-ind_vs_generic
mkdir -p $root_output_folder

for file in 2024-07-05_GBIC-Datasets-MOEBA/*-data.csv
do
    path=${file%-data.csv}
    id=$(basename $path)
    data_file=$file
    type_file=${path}-types.json

    mkdir -p ./$root_output_folder/$id/

    # 1. Individual
    out_folder=./$root_output_folder/$id/individual/
    mkdir -p $out_folder
    java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.Runner \
        --input-dataset $data_file \
        --input-column-types $type_file \
        --representation INDIVIDUAL \
        --str-fitness-functions "BiclusterSizeNormComp;MeanSquaredResidueNorm" \
        --population-size $population_size \
        --max-evaluations $num_evaluations \
        --str-algorithm NSGAII-AsyncParallel \
        --crossover-probability 0.9 \
        --mutation-probability 0.1 \
        --crossover-operator RowColUniformCrossover \
        --mutation-operator RowColUniformMutation \
        --observers FitnessEvolutionMinObserver \
        --num-threads 64 \
        --output-folder $out_folder > $out_folder/time_log.txt

    # 2. Generic
    out_folder=./$root_output_folder/$id/generic/
    mkdir -p $out_folder
    java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.Runner \
        --input-dataset $data_file \
        --input-column-types $type_file \
        --representation GENERIC \
        --str-fitness-functions "BiclusterSizeNormComp;MeanSquaredResidueNorm" \
        --population-size $population_size \
        --max-evaluations $num_evaluations \
        --str-algorithm NSGAII-AsyncParallel \
        --crossover-probability 0.9 \
        --mutation-probability 0.1 \
        --crossover-operator "PartiallyMappedCrossover;BicUniformCrossover;CellUniformCrossover" \
        --mutation-operator "SwapMutation;BicUniformMutation;CellUniformMutation" \
        --observers "FitnessEvolutionMinObserver;BiclusterCountObserver" \
        --num-threads 64 \
        --output-folder $out_folder \
        --summarise-individual-objectives HarmonicMean > $out_folder/time_log.txt

    # 3. Generic + (bSize -> bSizeNumBics)
    out_folder=./$root_output_folder/$id/generic-bSizeNumBics-0.25/
    mkdir -p $out_folder
    java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.Runner \
        --input-dataset $data_file \
        --input-column-types $type_file \
        --representation GENERIC \
        --str-fitness-functions "BiclusterSizeNumBicsNormComp(rowsWeight=0.5,coherenceWeight=0.25);MeanSquaredResidueNorm" \
        --population-size $population_size \
        --max-evaluations $num_evaluations \
        --str-algorithm NSGAII-AsyncParallel \
        --crossover-probability 0.9 \
        --mutation-probability 0.1 \
        --crossover-operator "PartiallyMappedCrossover;BicUniformCrossover;CellUniformCrossover" \
        --mutation-operator "SwapMutation;BicUniformMutation;CellUniformMutation" \
        --observers "FitnessEvolutionMinObserver;BiclusterCountObserver" \
        --num-threads 64 \
        --output-folder $out_folder \
        --summarise-individual-objectives HarmonicMean > $out_folder/time_log.txt

    # 4. Generic + DistanceBetweenBiclustersNormComp
    out_folder=./$root_output_folder/$id/generic+DistanceBetweenBiclusters/
    mkdir -p $out_folder
    java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.Runner \
        --input-dataset $data_file \
        --input-column-types $type_file \
        --representation GENERIC \
        --str-fitness-functions "BiclusterSizeNormComp;MeanSquaredResidueNorm;DistanceBetweenBiclustersNormComp" \
        --population-size $population_size \
        --max-evaluations $num_evaluations \
        --str-algorithm NSGAII-AsyncParallel \
        --crossover-probability 0.9 \
        --mutation-probability 0.1 \
        --crossover-operator "PartiallyMappedCrossover;BicUniformCrossover;CellUniformCrossover" \
        --mutation-operator "SwapMutation;BicUniformMutation;CellUniformMutation" \
        --observers "FitnessEvolutionMinObserver;BiclusterCountObserver" \
        --num-threads 64 \
        --output-folder $out_folder \
        --summarise-individual-objectives HarmonicMean > $out_folder/time_log.txt
    
    # 5. Generic + (bSize -> bSizeNumBics) + DistanceBetweenBiclustersNormComp
    out_folder=./$root_output_folder/$id/generic-bSizeNumBics-0.25+DistanceBetweenBiclusters/
    mkdir -p $out_folder
    java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.Runner \
        --input-dataset $data_file \
        --input-column-types $type_file \
        --representation GENERIC \
        --str-fitness-functions "BiclusterSizeNumBicsNormComp(rowsWeight=0.5,coherenceWeight=0.25);MeanSquaredResidueNorm;DistanceBetweenBiclustersNormComp" \
        --population-size $population_size \
        --max-evaluations $num_evaluations \
        --str-algorithm NSGAII-AsyncParallel \
        --crossover-probability 0.9 \
        --mutation-probability 0.1 \
        --crossover-operator "PartiallyMappedCrossover;BicUniformCrossover;CellUniformCrossover" \
        --mutation-operator "SwapMutation;BicUniformMutation;CellUniformMutation" \
        --observers "FitnessEvolutionMinObserver;BiclusterCountObserver" \
        --num-threads 64 \
        --output-folder $out_folder \
        --summarise-individual-objectives HarmonicMean > $out_folder/time_log.txt

    for output_folder in ./$root_output_folder/$id/*
    do
        translated_gold_standard=$path-translated.csv
        conf=$(basename $output_folder)
        representation=""
        if [[ $conf =~ ^generic ]]; 
        then
            representation="GENERIC"
        else
            representation="INDIVIDUAL"
        fi

        ## Plot results
        if [[ $representation == "GENERIC" ]]
        then
            python moebaoptresults2plot.py \
                --fun-file $output_folder/FUN.csv \
                --fitness-evolution-file $output_folder/FitnessEvolutionMinObserver.csv \
                --bicluster-count-file $output_folder/BiclusterCountObserver.csv \
                --population-size $population_size \
                --bicluster-count-plot-type box \
                --output-folder $output_folder/plots
        else
            python moebaoptresults2plot.py \
                --fun-file $output_folder/FUN.csv \
                --fitness-evolution-file $output_folder/FitnessEvolutionMinObserver.csv \
                --output-folder $output_folder/plots
        fi

        ## Comparison with gold standard
        ### Get metrics
        java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.validation.ValidationRunner \
            --inferred-translated $output_folder/VAR-translated.csv \
            --gold-standard-translated $translated_gold_standard \
            --validation-metrics "ScorePrelicRelevance;ScorePrelicRecovery;ScoreLiuWang;ScoreDice;ScoreAyadi;ScoreErenRelevance;ScoreErenRecovery;ClusteringErrorComplementary" \
            --representation $representation \
            --output-file $output_folder/validation/scores.csv 
        
        ### Get gold standard evaluation
        functions=$(head -n 1 $output_folder/FUN.csv | perl -pe 's/(,)(?=(?:[^()]*\([^()]*\))*[^()]*$)/;/g; s/"//g')
        java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.fitnessfunction.FastEvaluationRunner \
            --representation $representation \
            --input-dataset $data_file \
            --input-column-types $type_file \
            --solution-translated $translated_gold_standard \
            --str-fitness-functions "$functions" \
            --output-file $output_folder/validation/gs-FUN.csv \
            --summarise-individual-objectives HarmonicMean # Si la representación es individual simplemente será ignorado

        ### Plot comparison
        # Graphs
        python moebaoptresultsgoldstandard2plotcomparison.py \
            --var-translated-file $output_folder/VAR-translated.csv \
            --gold-standard-translated-file $translated_gold_standard \
            --metric intersection-size \
            --representation $representation \
            --plot-type graph \
            --output $output_folder/validation/plots
  

        # Pareto front
        num_objectives=$(echo $functions | awk -F';' '{print NF}')
        if [[ $num_objectives == "3" ]]
        then
            if [[ $representation == "GENERIC" ]]
            then
                python moebaoptresultsgoldstandard2plotcomparison.py \
                    --representation $representation \
                    --plot-type pareto-front-gs \
                    --accuracy-scores-file $output_folder/validation/scores.csv \
                    --gs-fun-file $output_folder/validation/gs-FUN.csv \
                    --fun-file $output_folder/FUN.csv \
                    --output $output_folder/validation/plots \
                    --metric ClusteringErrorComplementary 
            else
                python moebaoptresultsgoldstandard2plotcomparison.py \
                    --representation $representation \
                    --plot-type pareto-front-gs \
                    --accuracy-scores-file $output_folder/validation/scores.csv \
                    --gs-fun-file $output_folder/validation/gs-FUN.csv \
                    --fun-file $output_folder/FUN.csv \
                    --output $output_folder/validation/plots
            fi
        fi

        # Parallel coordinates
        if [[ $representation == "GENERIC" ]]
        then
            python moebaoptresultsgoldstandard2plotcomparison.py \
                --metric ClusteringErrorComplementary \
                --representation GENERIC \
                --plot-type evaluated-parallel-coordinates \
                --accuracy-scores-file $output_folder/validation/scores.csv \
                --gs-fun-file $output_folder/validation/gs-FUN.csv \
                --fun-file $output_folder/FUN.csv \
                --output $output_folder/validation/plots
        fi

        # Define a map (associative array) in Bash to store maximum and median
        declare -A stats

        # Read the first row to obtain the names of the columns
        IFS=',' read -r -a column_names < <(head -n 1 $output_folder/validation/scores.csv)

        # Read each column of the CSV file
        for col in $(seq 1 ${#column_names[@]})
        do
            # Extract the name of the column
            col_name="${column_names[$((col-1))]}"
            
            # Extract the data column (without heading)
            column_data=$(awk -F, -v col=$col '{print $col}' $output_folder/validation/scores.csv | tail -n +2)
            
            # Calculate the maximum
            max_value=$(echo "$column_data" | sort -gr | head -n 1)
            
            # Calculate the median
            sorted_column=$(echo "$column_data" | sort -g)
            count=$(echo "$sorted_column" | wc -l)
            
            if [ $((count % 2)) -eq 1 ]; then
                # If the number of rows is odd, the median is the central value
                mediana=$(echo "$sorted_column" | awk -v mid=$((count/2+1)) 'NR==mid')
            else
                # If the number of rows is even, the median is the average of the two central values
                val1=$(echo "$sorted_column" | awk -v mid=$((count/2)) 'NR==mid')
                val2=$(echo "$sorted_column" | awk -v mid=$((count/2+1)) 'NR==mid')
                mediana=$(echo "scale=8; ($val1+$val2)/2" | bc)
            fi
            
            # Store maximum and median in the associative array using the column name
            stats["$col_name-max"]=$max_value
            stats["$col_name-mediana"]=$mediana
        done

        # Order the keys alphabetically and store them in a list
        sorted_keys=($(for key in "${!stats[@]}"; do echo "$key"; done | sort))

        # Create strings with keys and values ​​in the same order
        sorted_keys_string=$(IFS=','; echo "${sorted_keys[*]}")
        sorted_values_string=$(IFS=','; for key in "${sorted_keys[@]}"; do echo -n "${stats[$key]},"; done | sed 's/,$//')

        # Extract the log time log
        milisegundos=$(grep -oP 'Total execution time: \K[0-9.]+(?=ms)' $output_folder/time_log.txt)

        # Move from milliseconds to minutes and seconds
        segundos=$((milisegundos / 1000))
        minutos=$((segundos / 60))
        segundos_restantes=$((segundos % 60))

        # Write the results in the CSV file
        if [ ! -f $root_output_folder/$id/config_comparison.csv ]; then
            echo "Configuration,Time,$sorted_keys_string" > $root_output_folder/$id/config_comparison.csv
        fi
        echo "$conf,$minutos min $segundos_restantes sec,$sorted_values_string" >> $root_output_folder/$id/config_comparison.csv

    done

done

# Destination directory for column output files
output_dir=$root_output_folder/ranking
mkdir -p $output_dir

first_file=true
first_line="Comparison"

# For each file config_comparison.csv in the directories
for comparison_file in $root_output_folder/*/config_comparison.csv; do

    # Identify the data set from the Board of Directors
    data_id=$(basename $(dirname $comparison_file))
    echo "Procesando $data_id"

    # Read the header to obtain the names of the columns
    header=$(head -n 1 $comparison_file)
    
    # Turn the heading into a list
    IFS=',' read -r -a column_names <<< "$header"

    # Read the content of the file and for each column of the metric, save it in a separate file
    if [[ $first_file == true ]]; then
        while IFS=',' read -r -a line; do
            config_name="${line[0]}" 
            first_line="${first_line},${config_name}"
        done < <(tail -n +2 $comparison_file)
    fi

    first_time=true
    while IFS=',' read -r -a line; do
        config_name="${line[0]}" 
        for i in "${!line[@]}"; do
            if [ "$i" -gt 0 ]; then 
                column_name="${column_names[$i]}"

                if [[ $first_file == true ]]; then
                    echo -n "$first_line" > "$output_dir/${column_name}.csv"
                fi
                if [[ $first_time == true ]]; then
                    echo >> "$output_dir/${column_name}.csv"
                    echo -n "$data_id,${line[$i]}" >> "$output_dir/${column_name}.csv"
                else 
                    echo -n ",${line[$i]}" >> "$output_dir/${column_name}.csv"
                fi
            fi
        done
        first_time=false
        first_file=false
    done < <(tail -n +2 $comparison_file)

done


cd control-test
for file in ../$output_dir/*.csv; do
    java Friedman $file > ${file%.csv}.txt
done
cd ..