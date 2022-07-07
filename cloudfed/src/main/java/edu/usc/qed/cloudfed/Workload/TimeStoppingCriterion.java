package edu.usc.qed.cloudfed.Workload;

import java.math.BigDecimal;

public class TimeStoppingCriterion implements StoppingCriterion {
    private BigDecimal tF;

    public TimeStoppingCriterion (BigDecimal tF) {
        this.tF = tF;
    }

    public boolean stop (BigDecimal t, int n) {
        return t.compareTo(tF) > 0;
    }
}