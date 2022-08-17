package edu.usc.qed.cloudfed.Workload;

import java.math.BigDecimal;
import java.io.IOException;

import org.msgpack.core.MessagePacker;
/*
 * Customizable workload stream
 */
public class WorkloadStream implements Comparable {
    private BigDecimal arrivalTime;
    private ArrivalProcess arrivalProcess;
    private BatchSizer batchSizer;
    private JobGenerator jobGenerator;
    public String streamLabel;

    public WorkloadStream (ArrivalProcess arrivalProcess, BatchSizer batchSizer, JobGenerator jobGenerator, String streamLabel) {
        this.arrivalProcess = arrivalProcess;
        this.batchSizer = batchSizer;
        this.jobGenerator = jobGenerator;
        arrivalTime = arrivalProcess.getInterarrivalTime();
        this.streamLabel = streamLabel;
    }
    
    public int compareTo(Object o) {
        return arrivalTime.compareTo(((WorkloadStream)o).arrivalTime);
    }

    public int writeBatch (MessagePacker packer) throws IOException {
        int n = batchSizer.getBatchSize();
        for (int i = 0; i < n; i++) {
            packer.packString(streamLabel);
            packer.packString(arrivalTime.toString());
            packer.packDouble(jobGenerator.getJobSize());
        }
        return n;
    }

    //Set next time
    public void update () {
        arrivalTime = arrivalTime.add(arrivalProcess.getInterarrivalTime());
    }
    
    //Get next time
    public BigDecimal getArrivalTime () {
        return arrivalTime;
    }
}
