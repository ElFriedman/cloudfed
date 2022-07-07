package edu.usc.qed.cloudfed.Workload;

import java.util.random.RandomGenerator;

public class DistBatchSizer implements BatchSizer {
    double[] probabilities;
    int[] batchSizes;
    RandomGenerator generator;

    //check they sum to 1?
    public DistBatchSizer (RandomGenerator generator, double[] probabilities, int[] batchSizes) {
        this.generator = generator;
        this.probabilities = probabilities;
        this.batchSizes = batchSizes;
    }

    public int getBatchSize () {
        double d = generator.nextDouble();
        int i = -1;
        while (d > 0) {
            i++;
            d -= probabilities[i];
        }
        return batchSizes[i];
    }
}
