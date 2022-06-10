package edu.usc.qed.cloudfed.CloudModel;

public class PoissonArrivalProcess implements ArrivalProcess {
    private double lambda;
    public PoissonArrivalProcess (double lambda) {
        this.lambda = lambda;
    }

    /**
     * Generates interarrival times from an exponential distribution
     * https://stackoverflow.com/a/5615564
     */

     // doesn't work for small and large lambdas
    public double getInterarrivalTime() {
        return -Math.log(1.0-Math.random())/lambda; 
    }

    

    //https://commons.apache.org/proper/commons-math/javadocs/api-3.5/org/apache/commons/math3/distribution/PoissonDistribution.html
    public double getInterarrivalTimeFromDistribution () {
        return 0;
    }
}
