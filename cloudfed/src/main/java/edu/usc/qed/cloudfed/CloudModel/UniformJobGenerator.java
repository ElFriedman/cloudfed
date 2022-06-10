package edu.usc.qed.cloudfed.CloudModel;
public class UniformJobGenerator implements JobGenerator {
    private double min, max;

    public UniformJobGenerator (double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getJobSize () {
        return Math.random() * (max - min) + min;
    }

}