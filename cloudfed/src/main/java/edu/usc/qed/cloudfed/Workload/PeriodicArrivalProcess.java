package edu.usc.qed.cloudfed.Workload;

import java.math.BigDecimal;

public class PeriodicArrivalProcess implements ArrivalProcess {
    private double lambda;

    public PeriodicArrivalProcess (double lambda) {
        this.lambda = lambda;
    }

    public BigDecimal getInterarrivalTime() {
        return new BigDecimal(1/lambda);
    }
}
