#!/bin/bash
source .venv/bin/activate

for problem in benchmark/*-data.csv
do
    preffix=${problem%"-data.csv"}
    id=$( basename $preffix )

    : '
    # Run MOEBA-HeCliDa with INDIVIDUAL representation
    evaluations=150000
    population_size=500
    output_folder=results-$id-I-bSbVMSR-$evaluations
    
    java -cp target/MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.Runner \
        --representation INDIVIDUAL \
        --input-dataset $preffix-data.csv \
        --input-column-types $preffix-types.json \
        --str-fitness-functions "BiclusterSizeNormComp;BiclusterVarianceNorm;MeanSquaredResidueNorm" \
        --population-size $population_size \
        --max-evaluations $evaluations \
        --str-algorithm NSGAII-AsyncParallel \
        --mutation-probability "0.3->0.05" \
        --observers "FitnessEvolutionAvgObserver;FitnessEvolutionMinObserver;NumEvaluationsObserver" \
        --crossover-operator "RowColUniformCrossover" \
        --mutation-operator "RowColUniformMutation" \
        --output-folder $output_folder

    ## Plot results
    python moebaoptresults2plot.py \
        --fun-file $output_folder/FUN.csv \
        --fitness-evolution-file $output_folder/FitnessEvolutionMinObserver.csv \
        --output-folder $output_folder/plots
    

    ## Comparison with gold standard
    ### Get metrics
    java -cp target/MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.validation.ValidationRunner \
        --inferred-translated $output_folder/VAR-translated.csv \
        --gold-standard-translated benchmark/$id-translated.csv \
        --validation-metrics "ScorePrelicRelevance;ScorePrelicRecovery;ScoreLiuWang;ScoreDice;ScoreAyadi;ScoreErenRelevance;ScoreErenRecovery;ClusteringErrorComplementary" \
        --representation INDIVIDUAL \
        --output-file $output_folder/validation/scores.csv \
        --save-process

    
    ### Get gold standard evaluation
    java -cp target/MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.fitnessfunction.FastEvaluationRunner \
        --representation INDIVIDUAL \
        --input-dataset $preffix-data.csv \
        --input-column-types $preffix-types.json \
        --solution-translated benchmark/$id-translated.csv \
        --str-fitness-functions "BiclusterSizeNormComp;BiclusterVarianceNorm;MeanSquaredResidueNorm" \
        --output-file $output_folder/validation/gs-FUN.csv \

    ### Plot comparison
    python moebaoptresultsgoldstandard2plotcomparison.py \
        --var-translated-file $output_folder/VAR-translated.csv \
        --gold-standard-translated-file benchmark/$id-translated.csv \
        --metric intersection-size \
        --representation INDIVIDUAL \
        --plot-type graph \
        --output $output_folder/validation/plots

    python moebaoptresultsgoldstandard2plotcomparison.py \
        --representation INDIVIDUAL \
        --plot-type pareto-front-gs \
        --accuracy-scores-file $output_folder/validation/scores.csv \
        --gs-fun-file $output_folder/validation/gs-FUN.csv \
        --fun-file $output_folder/FUN.csv \
        --output $output_folder/validation/plots
    
    
    # Run MOEBA-HeCliDa with GENERIC representation and grouped crossover
    evaluations=150000
    population_size=500
    output_folder=results-$id-G-GX-bSbVMSR-$evaluations

    java -cp target/MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.Runner \
            --representation GENERIC \
            --input-dataset $preffix-data.csv \
            --input-column-types $preffix-types.json \
            --str-fitness-functions "BiclusterSizeNormComp;BiclusterVarianceNorm;MeanSquaredResidueNorm" \
            --population-size $population_size \
            --max-evaluations $evaluations \
            --str-algorithm NSGAII-AsyncParallel \
            --mutation-probability "0.3->0.05" \
            --observers "BiclusterCountObserver;FitnessEvolutionAvgObserver;FitnessEvolutionMinObserver;NumEvaluationsObserver" \
            --crossover-operator "GroupedBasedCrossover(shuffleEnd=0.75,dynamicStartAmount=0.25);CellUniformCrossover" \
            --mutation-operator "SwapMutation;BicUniformMutation;CellUniformMutation" \
            --summarise-individual-objectives HarmonicMean \
            --generic-initial-min-num-bics 0 \
            --generic-initial-max-num-bics 250 \
            --output-folder $output_folder
    '

    algorithms=("MOCell-SingleThread" "SPEA2-SingleThread" "IBEA-SingleThread")
    for algorithm in "${algorithms[@]}"
    do
        evaluations=25000
        population_size=500
        output_folder=$algorithm-results-$id-G-GX-bSbVMSR-$evaluations
        java -cp target/MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.Runner \
            --representation GENERIC \
            --input-dataset $preffix-data.csv \
            --input-column-types $preffix-types.json \
            --str-fitness-functions "BiclusterSizeNormComp;BiclusterVarianceNorm;MeanSquaredResidueNorm" \
            --population-size $population_size \
            --max-evaluations $evaluations \
            --str-algorithm $algorithm \
            --mutation-probability "0.3->0.05" \
            --observers "BiclusterCountObserver;FitnessEvolutionAvgObserver;FitnessEvolutionMinObserver;NumEvaluationsObserver" \
            --crossover-operator "GroupedBasedCrossover(shuffleEnd=0.75,dynamicStartAmount=0.25);CellUniformCrossover" \
            --mutation-operator "SwapMutation;BicUniformMutation;CellUniformMutation" \
            --summarise-individual-objectives HarmonicMean \
            --generic-initial-min-num-bics 0 \
            --generic-initial-max-num-bics 250 \
            --output-folder $output_folder

        python moebaoptresults2plot.py \
            --fun-file $output_folder/FUN.csv \
            --fitness-evolution-file $output_folder/FitnessEvolutionMinObserver.csv \
            --bicluster-count-file $output_folder/BiclusterCountObserver.csv \
            --population-size $population_size \
            --bicluster-count-plot-type box \
            --output-folder $output_folder/plots
    done
    exit
    
    : '
    ## Plot results
    python moebaoptresults2plot.py \
        --fun-file $output_folder/FUN.csv \
        --fitness-evolution-file $output_folder/FitnessEvolutionMinObserver.csv \
        --bicluster-count-file $output_folder/BiclusterCountObserver.csv \
        --population-size $population_size \
        --bicluster-count-plot-type box \
        --output-folder $output_folder/plots

    ## Comparison with gold standard
    ### Get metrics
    java -cp target/MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.validation.ValidationRunner \
        --inferred-translated $output_folder/VAR-translated.csv \
        --gold-standard-translated benchmark/$id-translated.csv \
        --validation-metrics "ScorePrelicRelevance;ScorePrelicRecovery;ScoreLiuWang;ScoreDice;ScoreAyadi;ScoreErenRelevance;ScoreErenRecovery;ClusteringErrorComplementary" \
        --representation GENERIC \
        --output-file $output_folder/validation/scores.csv \
        --save-process
    
    ### Get gold standard evaluation
    java -cp target/MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.fitnessfunction.FastEvaluationRunner \
        --representation GENERIC \
        --input-dataset $preffix-data.csv \
        --input-column-types $preffix-types.json \
        --solution-translated benchmark/$id-translated.csv \
        --str-fitness-functions "BiclusterSizeNormComp;BiclusterVarianceNorm;MeanSquaredResidueNorm" \
        --summarise-individual-objectives HarmonicMean \
        --output-file $output_folder/validation/gs-FUN.csv \

    ### Plot comparison
    python moebaoptresultsgoldstandard2plotcomparison.py \
        --var-translated-file $output_folder/VAR-translated.csv \
        --gold-standard-translated-file benchmark/$id-translated.csv \
        --metric intersection-size \
        --representation GENERIC \
        --plot-type graph \
        --output $output_folder/validation/plots
    

    python moebaoptresultsgoldstandard2plotcomparison.py \
        --metric ClusteringErrorComplementary \
        --representation GENERIC \
        --plot-type evaluated-parallel-coordinates \
        --accuracy-scores-file $output_folder/validation/scores.csv \
        --gs-fun-file $output_folder/validation/gs-FUN.csv \
        --fun-file $output_folder/FUN.csv \
        --output $output_folder/validation/plots

    python moebaoptresultsgoldstandard2plotcomparison.py \
        --metric ClusteringErrorComplementary \
        --representation GENERIC \
        --plot-type pareto-front-gs \
        --accuracy-scores-file $output_folder/validation/scores.csv \
        --gs-fun-file $output_folder/validation/gs-FUN.csv \
        --fun-file $output_folder/FUN.csv \
        --output $output_folder/validation/plots

    '
done