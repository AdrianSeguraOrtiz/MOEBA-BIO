package moeba.fitnessfunction;

import java.util.ArrayList;

import moeba.StaticUtils;
import moeba.utils.storage.CacheStorage;

public abstract class IndividualFitnessFunction extends FitnessFunction {
    
    public IndividualFitnessFunction(double[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache, String summariseIndividualObjectives) {
        super(data, types, internalCache);

        if (summariseIndividualObjectives == null) {
            this.func = internalCache == null ? this::runMeanWithoutCache : this::runMeanWithCache;
        } else {
            switch (summariseIndividualObjectives.toLowerCase()) {
                case "mean":
                    this.func = internalCache == null ? this::runMeanWithoutCache : this::runMeanWithCache;
                    break;
                case "harmonicmean":
                    this.func = internalCache == null ? this::runHarmonicMeanWithoutCache : this::runHarmonicMeanWithCache;
                    break;
                case "geometricmean":
                    this.func = internalCache == null ? this::runGeometricMeanWithoutCache : this::runGeometricMeanWithCache;
                    break;
                default:
                    throw new IllegalArgumentException("Summarise method not supported: " + summariseIndividualObjectives);
            }
        }
    }

    @Override
    public double run(ArrayList<ArrayList<Integer>[]> biclusters) {
        return 1 - super.func.run(biclusters);
    }

    protected double runMeanWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        for (ArrayList<Integer>[] bic : biclusters) {
            res += getBiclusterScore(bic);
        }
        return res/biclusters.size();
    }

    protected double runMeanWithCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        String key;
        double bicScore;
        for (ArrayList<Integer>[] bic : biclusters) {
            key = StaticUtils.biclusterToString(bic);
            if (internalCache.containsKey(key)){
                bicScore = internalCache.get(key);
            } else {
                bicScore = getBiclusterScore(bic);
                internalCache.put(key, bicScore);
            }
            res += bicScore;
        }
        return res/biclusters.size();
    }

    protected double runHarmonicMeanWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        for (ArrayList<Integer>[] bic : biclusters) {
            res += 1 / getBiclusterScore(bic);
        }
        return biclusters.size()/res;
    }

    protected double runHarmonicMeanWithCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        String key;
        double bicScore;
        for (ArrayList<Integer>[] bic : biclusters) {
            key = StaticUtils.biclusterToString(bic);
            if (internalCache.containsKey(key)){
                bicScore = internalCache.get(key);
            } else {
                bicScore = getBiclusterScore(bic);
                internalCache.put(key, bicScore);
            }
            res += 1 / bicScore;
        }
        return biclusters.size()/res;
    }

    protected double runGeometricMeanWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 1;
        for (ArrayList<Integer>[] bic : biclusters) {
            res *= getBiclusterScore(bic);
        }
        return Math.pow(res, (double) 1 / biclusters.size());
    }

    protected double runGeometricMeanWithCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 1;
        String key;
        double bicScore;
        for (ArrayList<Integer>[] bic : biclusters) {
            key = StaticUtils.biclusterToString(bic);
            if (internalCache.containsKey(key)){
                bicScore = internalCache.get(key);
            } else {
                bicScore = getBiclusterScore(bic);
                internalCache.put(key, bicScore);
            }
            res *= bicScore;
        }
        return Math.pow(res, (double) 1 / biclusters.size());
    }

    protected abstract double getBiclusterScore(ArrayList<Integer>[] bicluster);
}
