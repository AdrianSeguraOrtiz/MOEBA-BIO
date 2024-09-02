import pandas as pd
from sklearn.preprocessing import KBinsDiscretizer
import argparse
from biclustlib.algorithms import *
from biclustlib.algorithms.wrappers import *
from biclustlib.models import Biclustering

def discretize_data(raw_data: pd.DataFrame, n_bins: int = 2) -> pd.DataFrame:
    return pd.DataFrame(KBinsDiscretizer(n_bins, encode='ordinal', strategy='kmeans').fit_transform(raw_data),
                        index=raw_data.index).astype(int if n_bins > 2 else bool)

def load_data(file_path: str) -> pd.DataFrame:
    return pd.read_csv(file_path, index_col=0)

def save_biclusters(file_path: str, biclusters: Biclustering) -> None:
    with open(file_path, 'w') as f:
        for i in range(len(biclusters.biclusters)):
            rows = ' '.join(str(x) for x in biclusters.biclusters[i].rows)
            cols = ' '.join(str(x) for x in biclusters.biclusters[i].cols)
            f.write(f"Bicluster{i}: (rows: [{rows}] cols: [{cols}])")
            if i < len(biclusters.biclusters) - 1:
                f.write(', ')

def competitors(algorithm: str, input_file: str, output_file: str) -> None:
    data = load_data(args.input_file)
    n_biclusters = max(1, int(0.1 * (data.shape[0] + data.shape[1]) / 2))
    discretion_level = 30

    data_dis = discretize_data(data, discretion_level)
    data_bin = discretize_data(data)

    setup = {
        'CCA': (ChengChurchAlgorithm(n_biclusters), data),
        'Bimax': (RBinaryInclusionMaximalBiclusteringAlgorithm(n_biclusters), data_bin),
        'ISA': (IterativeSignatureAlgorithm2(), data),
        'OPSM': (OrderPreservingSubMatrix(), data),
        'Plaid': (RPlaid(n_biclusters), data),
        'BiBit': (BitPatternBiclusteringAlgorithm(), data_bin),
        'xMotifs': (RConservedGeneExpressionMotifs(n_biclusters), data_dis),
        'LAS': (LargeAverageSubmatrices(n_biclusters), data),
        'Spectral': (Spectral(n_clusters=data.shape[1] // 2), data + abs(data.min().min()) + 1),
    }

    alg, data = setup[algorithm]
    bics = alg.run(data)
    save_biclusters(output_file, bics)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Run a biclustering algorithm on input data.')
    parser.add_argument('--algorithm', choices=['CCA', 'Bimax', 'ISA', 'OPSM', 'Plaid', 'BiBit', 'xMotifs', 'LAS', 'Spectral'],
                        help='The algorithm to run')
    parser.add_argument('--input-file', help='Path to the input CSV file')
    parser.add_argument('--output-file', help='Path to the output file to save biclusters')

    args = parser.parse_args()
    
    competitors(args.algorithm, args.input_file, args.output_file)

    
