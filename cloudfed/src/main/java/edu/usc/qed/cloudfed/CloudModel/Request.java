package edu.usc.qed.cloudfed.CloudModel;

public class Request implements Comparable {
    private double time; 
    private double jobSize;
    private boolean inService;
    // if inService is false, time is arrivalTime
    // if inService is true, time is finishTime

    public Request (double arrTime, double jobSize) {
        time = arrTime;
        this.jobSize = jobSize;
        inService = false;
        
    }

    public void service (double newTime) {
        if (inService) {
            System.out.println("error: request should not be put into service if already" +
            " in service");
        }
        inService = true;
        time = newTime;
    }

    public boolean inService () {
        return inService;
    }

    public int compareTo (Object o) {
        return (int)(((Request) o).time - time); //CHECK
    }
}
