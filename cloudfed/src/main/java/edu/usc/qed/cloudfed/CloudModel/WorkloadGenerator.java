package edu.usc.qed.cloudfed.CloudModel;

import java.math.BigDecimal;

//SERIALIZEEEE both times and jobs

public class WorkloadGenerator {
    private ArrivalProcess arrProcess;
    private JobGenerator jGenerator;

    public WorkloadGenerator (ArrivalProcess arrivalProcess, JobGenerator jobGenerator) {
        this.arrProcess = arrivalProcess;
        this.jGenerator = jobGenerator;
    }

    public Request generateRequest (BigDecimal time) {
        BigDecimal iaTime = time.add(new BigDecimal(arrProcess.getInterarrivalTime()));
        return new Request (iaTime, jGenerator.getJobSize());
    }
}