package edu.usc.qed.cloudfed.Workload;

import java.math.BigDecimal;
import java.util.random.RandomGenerator;

public class PoissonArrivalProcess implements ArrivalProcess {
    private double lambda;
    private RandomGenerator generator;

    public PoissonArrivalProcess (RandomGenerator generator, double lambda) {
        this.lambda = lambda;
        this.generator = generator;
    }

    public BigDecimal getInterarrivalTime() {
        return new BigDecimal(generator.nextExponential()/lambda);
    }
}
