package edu.usc.qed.cloudfed.Workload;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.math.BigDecimal;
import java.io.IOException;

import org.msgpack.core.MessagePacker;

/*
 * Create a workload with multiple streams
 */
public class WorkloadGenerator {
    private PriorityQueue<WorkloadStream> streamPQ;

    public WorkloadGenerator (ArrayList<WorkloadStream> streams) {
        streamPQ = new PriorityQueue<WorkloadStream>();
        for (WorkloadStream stream : streams) {
            streamPQ.add(stream);
        }
    }

    public void generateWorkload (MessagePacker packer, StoppingCriterion stoppingCriterion) throws IOException {
        BigDecimal t = new BigDecimal(-1);
        int jobCount = 0;
        while (!stoppingCriterion.stop(t, jobCount)) {
            WorkloadStream stream = streamPQ.poll();
            t = stream.getArrivalTime();
            jobCount += stream.writeBatch(packer);
            stream.update();
            streamPQ.add(stream);
        }
    }
}