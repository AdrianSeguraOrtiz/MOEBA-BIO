total_mem=$(free --kilo | awk '/^Mem:/ {print $2}')
xmx=$((total_mem * 95 / 100))
java -cp MOEBA-1.0-SNAPSHOT-jar-with-dependencies.jar moeba.parameterization.ParameterizationRunner \
     --input-benchmark-folder fabia_simulated_data \
     --internal-evaluations 25000 \
     --unsupervised-conf-file parameterization/co_expression_unsupervised.csv \
     --external-unsupervised-evaluations 1500 \
     --external-unsupervised-population-size 50 \
     --supervised-conf-file parameterization/co_expression_supervised.csv \
     --external-supervised-evaluations 2000 \
     --external-supervised-population-size 50