package edu.usc.qed.cloudfed.CloudModel;

//SERIALIZEEEE both times and jobs

public class WorkloadGenerator {
    private ArrivalProcess arrProcess;
    private JobGenerator jGenerator;

    public WorkloadGenerator (ArrivalProcess arrivalProcess, JobGenerator jobGenerator) {
        this.arrProcess = arrivalProcess;
        this.jGenerator = jobGenerator;
    }

    public Request generateRequest (double currTime) {
        return new Request (currTime + arrProcess.getInterarrivalTime(), jGenerator.getJobSize());
    }
}