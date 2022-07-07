package edu.usc.qed.cloudfed.Workload;

import java.math.BigDecimal;

public class JobStoppingCriterion implements StoppingCriterion {
    private int jobs;

    public JobStoppingCriterion (int jobs) {
        this.jobs = jobs;
    }

    public boolean stop (BigDecimal t, int n) {
        return n >= jobs;
    }
}
