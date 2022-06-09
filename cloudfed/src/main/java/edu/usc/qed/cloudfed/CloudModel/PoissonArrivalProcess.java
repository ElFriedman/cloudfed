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

     // DOES NOT NECESSARILY WORK FOR LARGE AND SMALL LAMBDAS????
    public double getInterarrivalTime() {
        return -Math.log(1.0-Math.random())/lambda; 
    }

    public double getInterarrivalTimeEdgeCase () {

    }
}
