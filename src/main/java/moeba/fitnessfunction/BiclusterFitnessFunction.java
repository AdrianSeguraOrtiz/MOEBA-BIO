package moeba.fitnessfunction;

import java.util.ArrayList;

import moeba.StaticUtils;
import moeba.utils.storage.CacheStorage;

public abstract class BiclusterFitnessFunction extends FitnessFunction {

    protected CacheStorage<String, Double> internalCache;

    public BiclusterFitnessFunction(double[][] data, Class<?>[] types, CacheStorage<String, Double> internalCache, String summariseIndividualObjectives) {
        super(data, types);
        this.internalCache = internalCache;

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
        int n = biclusters.size();
        for (int i = 0; i < n; i++) {
            res += getBiclusterScore(biclusters, i);
        }
        return res / n;
    }

    protected double runMeanWithCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        String key;
        double bicScore;
        int n = biclusters.size();
        for (int i = 0; i < n; i++) {
            key = StaticUtils.biclusterToString(biclusters.get(i));
            if (internalCache.containsKey(key)){
                bicScore = internalCache.get(key);
            } else {
                bicScore = getBiclusterScore(biclusters, i);
                internalCache.put(key, bicScore);
            }
            res += bicScore;
        }
        return res / n;
    }

    protected double runHarmonicMeanWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        int n = biclusters.size();
        for (int i = 0; i < n; i++) {
            res += 1 / getBiclusterScore(biclusters, i);
        }
        return n / res;
    }

    protected double runHarmonicMeanWithCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 0;
        String key;
        double bicScore;
        int n = biclusters.size();
        for (int i = 0; i < n; i++) {
            key = StaticUtils.biclusterToString(biclusters.get(i));
            if (internalCache.containsKey(key)){
                bicScore = internalCache.get(key);
            } else {
                bicScore = getBiclusterScore(biclusters, i);
                internalCache.put(key, bicScore);
            }
            res += 1 / bicScore;
        }
        return n / res;
    }

    protected double runGeometricMeanWithoutCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 1;
        int n = biclusters.size();
        for (int i = 0; i < n; i++) {
            res *= getBiclusterScore(biclusters, i);
        }
        return Math.pow(res, (double) 1 / n);
    }

    protected double runGeometricMeanWithCache(ArrayList<ArrayList<Integer>[]> biclusters) {
        double res = 1;
        String key;
        double bicScore;
        int n = biclusters.size();
        for (int i = 0; i < n; i++) {
            key = StaticUtils.biclusterToString(biclusters.get(i));
            if (internalCache.containsKey(key)){
                bicScore = internalCache.get(key);
            } else {
                bicScore = getBiclusterScore(biclusters, i);
                internalCache.put(key, bicScore);
            }
            res *= bicScore;
        }
        return Math.pow(res, (double) 1 / n);
    }

    protected abstract double getBiclusterScore(ArrayList<ArrayList<Integer>[]> biclusters, int i);
    
}
