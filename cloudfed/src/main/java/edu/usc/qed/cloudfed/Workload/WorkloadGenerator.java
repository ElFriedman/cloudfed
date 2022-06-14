package edu.usc.qed.cloudfed.Workload;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.math.BigDecimal;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

public class WorkloadGenerator {
    private PriorityQueue<WorkloadStream> streamPQ;

    public WorkloadGenerator (ArrayList<WorkloadStream> streams) {
        streamPQ = new PriorityQueue<WorkloadStream>();
        for (WorkloadStream stream : streams) {
            streamPQ.add(stream);
        }
    }

    public void generateWorkload (String fileName, StoppingCriterion stoppingCriterion) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(new File (fileName)));
        BigDecimal t = new BigDecimal(-1);
        int jobCount = 0;
        while (!stoppingCriterion.stop(t, jobCount)) {
            WorkloadStream stream = streamPQ.poll();
            t = stream.getArrivalTime();
            jobCount += stream.writeBatch(out);
            stream.update();
            streamPQ.add(stream);
        }
        out.close();
    }
}