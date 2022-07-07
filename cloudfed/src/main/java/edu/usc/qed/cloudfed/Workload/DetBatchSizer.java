package edu.usc.qed.cloudfed.Workload;

public class DetBatchSizer implements BatchSizer {
    private int batchSize;

    public DetBatchSizer (int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchSize () {
        return batchSize;
    }
}
