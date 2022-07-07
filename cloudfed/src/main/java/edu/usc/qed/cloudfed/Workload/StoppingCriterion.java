package edu.usc.qed.cloudfed.Workload;

import java.math.BigDecimal;
public interface StoppingCriterion {
    public boolean stop(BigDecimal t, int n);
}
