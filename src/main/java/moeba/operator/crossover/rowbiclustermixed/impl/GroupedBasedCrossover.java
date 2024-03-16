package moeba.operator.crossover.rowbiclustermixed.impl;

import java.util.BitSet;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

import moeba.operator.crossover.rowbiclustermixed.RowBiclusterMixedCrossover;
import org.uma.jmetal.solution.integersolution.IntegerSolution;

public class GroupedBasedCrossover implements RowBiclusterMixedCrossover {
    private Random random;
    private AtomicInteger numOperations;
    private int numApproxCrossovers;
    private float shuffleEnd;
    private float dynamicStartAmount;

    public GroupedBasedCrossover(int numApproxCrossovers, float shuffleEnd, float dynamicStartAmount) {
        this.numApproxCrossovers = numApproxCrossovers;
        this.shuffleEnd = shuffleEnd;
        this.dynamicStartAmount = dynamicStartAmount;
        this.random = new Random();
        this.numOperations = new AtomicInteger();
    }

    public GroupedBasedCrossover(int numApproxCrossovers, float shuffleEnd, float dynamicStartAmount, Random random) {
        this.numApproxCrossovers = numApproxCrossovers;
        this.shuffleEnd = shuffleEnd;
        this.dynamicStartAmount = dynamicStartAmount;
        this.random = random;
        this.numOperations = new AtomicInteger();
    }

    @Override
    public void execute(IntegerSolution is1, IntegerSolution is2, BitSet bs1, BitSet bs2) {
        
        // Calculamos el porcentaje de operaciones de cruce realizados hasta ahora
        float doned = (float) numOperations.getAndIncrement() / this.numApproxCrossovers;

        // En función del porcentaje de operaciones, calculamos el porcentaje de biclusters que vamos a cruzar
        float amount = (1 - dynamicStartAmount) * doned + dynamicStartAmount;

        // Marcamos el último bit de cada individuo a true para poder igualar su cardinalidad al número de biclusters
        int n = is1.variables().size();
        bs1.set(n-1);
        bs2.set(n-1);

        // El número de biclusters a cruzar para cada individuo es el 'amount' porciento del total del individuo, siendo como mínimo 1
        int numBicsP1 = Math.max((int) (bs1.cardinality() * amount), 1);
        int numBicsP2 = Math.max((int) (bs2.cardinality() * amount), 1);

        // Obtenemos el rango de posiciones que ocupa el conjunto de biclusters a cruzar
        int[] limits1 = amount != 1 ? getLimits(bs1, random.nextInt(n-1)+1, numBicsP1, n) : new int[] {0, n};
        int[] limits2 = amount != 1 ? getLimits(bs2, random.nextInt(n-1)+1, numBicsP2, n) : new int[] {0, n};
        
        // El vector bics almacena en la posición i el identificador del bicluster al que pertenece la fila i. Si la fila i ha quedado fuera del rango, se le asignará el valor 0
        // El vector cuts almacena en la posición i el punto de corte del bicluster i. Dado que el bicluster 0 representa las sobras fuera de rango, se dejará en cuts[0] el valor por defecto 0
        // Para el individuo 1:
        int b1 = 1;
        int[] bicsP1 = new int[n];
        int[] cutsP1 = new int[numBicsP1 + 1];
        for (int i = limits1[0]; i < limits1[1]; i++) {
            bicsP1[is1.variables().get(i)] = b1;
            if (bs1.get(i)) {
                cutsP1[b1] = i;
                b1 += 1;
            }
        }

        // Para el individuo 2:
        int b2 = 1;
        int[] bicsP2 = new int[n];
        int[] cutsP2 = new int[numBicsP2 + 1];
        for (int i = limits2[0]; i < limits2[1]; i++) {
            bicsP2[is2.variables().get(i)] = b2;
            if (bs2.get(i)) {
                cutsP2[b2] = i;
                b2 += 1;
            }
        }

        // En la matriz matches se almacena en la fila i columna j el número de filas que tiene en comun el bicluster i en el individuo principal y el bicluster j en el individuo complementario
        int[][] matchesP1 = new int[numBicsP1+1][numBicsP2+1];
        int[][] matchesP2 = new int[numBicsP2+1][numBicsP1+1];
        for (int i = 0; i < n; i++) {
            matchesP1[bicsP1[i]][bicsP2[i]]++;
            matchesP2[bicsP2[i]][bicsP1[i]]++;
        }

        // Para cada bicluster finalmente se escoge el bicluster del complementario que más filas tiene en común (excluyendo el bicluster 0 con los sobrantes fuera de rango)
        int[] bestMatchesP1 = getBestMatches(matchesP1);
        int[] bestMatchesP2 = getBestMatches(matchesP2);

        // En el vector visited se almacenan las filas ya añadidas a la solución para asegurar el mantenimiento de la permutación. En este caso se inicializa poniendo a true las filas que no participan en el cruce y que se quedan fuera del rango
        // Para el individuo 1:
        boolean[] visitedO1 = new boolean[n];
        for (int i = 0; i < limits1[0]; i++) {
            visitedO1[is1.variables().get(i)] = true;
        }
        for (int i = limits1[1]; i < n; i++) {
            visitedO1[is1.variables().get(i)] = true;
        }

        // Para el individuo 2:
        boolean[] visitedO2 = new boolean[n];
        for (int i = 0; i < limits2[0]; i++) {
            visitedO2[is2.variables().get(i)] = true;
        }
        for (int i = limits2[1]; i < n; i++) {
            visitedO2[is2.variables().get(i)] = true;
        }

        // Se extraen copias del contenido genético de los padres en los rangos a cruzar
        int[] p1 = is1.variables().stream().skip(limits1[0]).limit(limits1[1] - limits1[0]).mapToInt(Integer::intValue).toArray();
        int[] p2 = is2.variables().stream().skip(limits2[0]).limit(limits2[1] - limits2[0]).mapToInt(Integer::intValue).toArray();

        // Reseteamos los bits de la zona de actuación y actualizamos la permutación agrupando matches
        bs1.clear(limits1[0], limits1[1]);
        updateSolutions(is1, limits1[0], limits2[0], p1, p2, bs1, cutsP1, cutsP2, bestMatchesP1, visitedO1, doned);
        bs2.clear(limits2[0], limits2[1]);
        updateSolutions(is2, limits2[0], limits1[0], p2, p1, bs2, cutsP2, cutsP1, bestMatchesP2, visitedO2, doned);
    }

    public int[] getLimits (BitSet bs, int seed, int numBics, int n) {
        int[] res = new int[2];

        int nextStart;
        int nextEnd;
        res[0] = seed;
        res[1] = seed;
        // Si la semilla cae en un 1, cuando se logre la primera expansión por ambos lados se generan 2 biclusters, por lo tanto está bien en 0
        // Si la semilla cae en un 0, cuando se logre la primera expansión por ambos lados se genera 1 solo bicluster, por lo que lo iniciamos a -1 para que cuando se sumen se quede a 1
        int nb = bs.get(seed) ? 0 : -1;
        while (nb < numBics) {
            nextStart = bs.previousSetBit(res[0]-1);
            nextEnd = bs.nextSetBit(res[1]+1);
            if (nb % 2 == 0) {
                // Si el bit anterior puesto a true es distinto de -1, se ha añadido un bicluster por detrás
                if (nextStart != -1) {
                    res[0] = nextStart;
                } 
                // Si es -1 por primera vez significa que hemos llegado al primer 1 o que la semilla ha caido entre el inicio y el primer 1
                // Si la semilla ha caido entre el inicio y el primer 1, se añade el primer bicluster para no coger solo una parte
                else if (res[0] == seed) {
                    res[0] = 0;
                } 
                // Si el bit siguiente es distinto de -1, se ha anadido un bicluster por delante
                else if (nextEnd != -1) {
                    res[1] = nextEnd;
                }
            } else {
                // Igual que antes pero dándole prioridad a la cola
                if (nextEnd != -1) {
                    res[1] = nextEnd;
                } else if (nextStart != -1) {
                    res[0] = nextStart;
                } else if (res[0] == seed) {
                    res[0] = 0;
                }
            }
            nb++;
        }
        res[0]++;
        if (res[1] != n) res[1]++;

        return res;
    }
    
    public int[] getBestMatches(int[][] matches) {
        int[] bestMatches = new int[matches.length];
        int max, sum;
        for (int i = 1; i < matches.length; i++) {
            max = 0;
            sum = 0;
            for (int j = 1; j < matches[0].length; j++) {
                sum += matches[i][j];
                if (matches[i][j] >= max) {
                    max = matches[i][j];
                    bestMatches[i] = j;
                }
                if (max > (matches[0].length - sum) / 2) {
                    break;
                }
            }
        }
        return bestMatches;
    }

    public void updateSolutions(IntegerSolution is, int start, int startComp, int[] p, int[] pComp, BitSet bs, int[] cuts, int[] cutsComp, int[] bestMatches, boolean[] visited, float doned) {
        int bm;
        int cut, prevCut = start;
        int cutComp, prevCutComp = 0;
        int cnt = 0;
        float r;
        int numRows;
        ArrayList<Integer> rows = new ArrayList<>();
        for (int b = 1; b < bestMatches.length; b++){
            bm = bestMatches[b];
            cut = cuts[b];
            cutComp = cutsComp[bm];
            prevCutComp = bm == 1 ? startComp : cutsComp[bm-1];

            for (int j = prevCut - start; j <= cut - start; j++) {
                if (!visited[p[j]]) {
                    rows.add(p[j]);
                    visited[p[j]] = true;
                }
            }
            for (int j = prevCutComp - startComp; j <= cutComp - startComp; j++) {
                if (!visited[pComp[j]]) {
                    rows.add(pComp[j]);
                    visited[pComp[j]] = true;
                }
            }

            if (doned < shuffleEnd) Collections.shuffle(rows);
            numRows = rows.size();
            for (int j = 0; j < numRows; j++) {
                is.variables().set(start + cnt + j, rows.get(j));
            }
            r = random.nextInt(3);
            bs.set(start + cnt + numRows);
            if (r == 0) {
                bs.set(start + cnt + numRows/3);
                bs.set(start + cnt + 2*numRows/3);
            } else if (r == 1) {
                bs.set(start + cnt + numRows/2);
            }
            cnt += numRows;
            prevCut = cut + 1;
            rows.clear();
        }
    }
    
}
