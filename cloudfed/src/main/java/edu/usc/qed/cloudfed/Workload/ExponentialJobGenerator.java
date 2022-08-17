package edu.usc.qed.cloudfed.Workload;

import java.util.random.RandomGenerator;

public class ExponentialJobGenerator implements JobGenerator {
    private double lambda;
    private RandomGenerator generator;

    public ExponentialJobGenerator (RandomGenerator generator, double lambda) {
        this.lambda = lambda;
        this.generator = generator;
    }

    public double getJobSize () {
        return generator.nextExponential()/lambda;
    }

    public double meanJobSize () {
        return 1/lambda;
    }
}