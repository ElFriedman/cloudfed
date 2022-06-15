package edu.usc.qed.cloudfed.Workload;

import java.math.BigDecimal;
import java.io.IOException;

import org.msgpack.core.MessagePacker;

public class WorkloadStream implements Comparable {
    private BigDecimal arrivalTime;
    private ArrivalProcess arrivalProcess;
    private BatchSizer batchSizer;
    private JobGenerator jobGenerator;

    public WorkloadStream (ArrivalProcess arrivalProcess, BatchSizer batchSizer, JobGenerator jobGenerator) {
        this.arrivalProcess = arrivalProcess;
        this.batchSizer = batchSizer;
        this.jobGenerator = jobGenerator;
        arrivalTime = arrivalProcess.getInterarrivalTime();
    }
    
    public int compareTo(Object o) {
        return arrivalTime.compareTo(((WorkloadStream)o).arrivalTime);
    }

    public int writeBatch (MessagePacker packer) throws IOException {
        int n = batchSizer.getBatchSize();
        for (int i = 0; i < n; i++) {
            packer.packString(arrivalTime.toString());
            packer.packDouble(jobGenerator.getJobSize());
        }
        return n;
    }

    public void update () {
        arrivalTime = arrivalTime.add(arrivalProcess.getInterarrivalTime());
    }
    
    public BigDecimal getArrivalTime () {
        return arrivalTime;
    }
}
