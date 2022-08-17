package edu.usc.qed.cloudfed.Workload;

import org.apache.commons.math3.random.RandomGenerator;

public class ApacheRandomGenerator implements RandomGenerator {
    private java.util.random.RandomGenerator rng;

    public ApacheRandomGenerator (java.util.random.RandomGenerator rng) {
        this.rng = rng;
    }

    @Override
    public void setSeed(int seed) {
        System.out.println("error shouldn't be setting apache random seed");
    }

    @Override
    public void setSeed(int[] seed) {
        System.out.println("error shouldn't be setting apache random seed");
        
    }

    @Override
    public void setSeed(long seed) {
        System.out.println("error shouldn't be setting apache random seed");
        
    }

    @Override
    public void nextBytes(byte[] bytes) {
        rng.nextBytes(bytes);
    }

    @Override
    public int nextInt() {
        return rng.nextInt();
    }

    @Override
    public int nextInt(int n) {
        return rng.nextInt(n);
    }

    @Override
    public long nextLong() {
        return rng.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return rng.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return rng.nextFloat();
    }

    @Override
    public double nextDouble() {
        return rng.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return rng.nextGaussian();
    }
    
}
