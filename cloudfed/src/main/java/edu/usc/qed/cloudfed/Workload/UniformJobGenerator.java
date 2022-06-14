package edu.usc.qed.cloudfed.Workload;

import java.util.random.RandomGenerator;

public class UniformJobGenerator implements JobGenerator {
    private double min, max;
    private RandomGenerator generator;

    public UniformJobGenerator (RandomGenerator generator, double min, double max) {
        this.min = min;
        this.max = max;
        this.generator = generator;
    }

    public double getJobSize () {
        return generator.nextDouble() * (max - min) + min;
    }

}