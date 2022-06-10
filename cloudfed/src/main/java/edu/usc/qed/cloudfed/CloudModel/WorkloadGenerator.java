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

    public Request generateRequest (BigDecimal currTime) {
        BigDecimal iaTime = new BigDecimal(arrProcess.getInterarrivalTime());
        iaTime.add(currTime);
        return new Request (iaTime, jGenerator.getJobSize());
    }
}