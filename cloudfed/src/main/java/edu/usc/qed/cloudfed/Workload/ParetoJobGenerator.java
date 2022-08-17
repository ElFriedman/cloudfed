package edu.usc.qed.cloudfed.Workload;

import java.util.random.RandomGenerator;

import org.apache.commons.math3.distribution.ParetoDistribution;

public class ParetoJobGenerator implements JobGenerator {
    private ParetoDistribution generator;

    public ParetoJobGenerator (RandomGenerator rng, double scale, double shape) {
        generator = new ParetoDistribution(new ApacheRandomGenerator(rng), scale, shape);
    }

    public double getJobSize () {
        return generator.sample();
    }

    public double meanJobSize () {
        return generator.getNumericalMean();
    }
}