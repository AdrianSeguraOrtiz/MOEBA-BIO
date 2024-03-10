package moeba.utils.observer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import moeba.utils.observer.ProblemObserver.ObserverInterface;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

public class NumEvaluationsObserver implements ObserverInterface {
    private int populationSize;
    private AtomicInteger parallelCount;
    private ArrayList<Integer> evaluations;

    public NumEvaluationsObserver(int populationSize) {
        this.populationSize = populationSize;
        this.parallelCount = new AtomicInteger();
        this.evaluations = new ArrayList<>();
    }

    @Override
    public void register(CompositeSolution result) {
        int cnt = this.parallelCount.incrementAndGet();
        if (cnt % this.populationSize == 0) {
            System.out.println(cnt);
            this.evaluations.add(cnt);
        }
    }

    @Override
    public void writeToFile(String strFile) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(strFile))) {
            String strVector = Arrays.toString(this.evaluations.toArray());
            bw.write(strVector.substring(1, strVector.length() - 1) + "\n");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    
}
