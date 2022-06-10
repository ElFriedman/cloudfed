package edu.usc.qed.cloudfed.CloudModel;
import java.math.BigDecimal; //add this to the cloudfed package

public class Request implements Comparable {
    private BigDecimal time; 
    private double jobSize;
    private boolean inService;
    // if inService is false, time is arrivalTime
    // if inService is true, time is finishTime

    public Request (BigDecimal arrTime, double jobSize) {
        time = arrTime;
        this.jobSize = jobSize;
        inService = false;
    }

    public void service (BigDecimal newTime) {
        if (inService) {
            System.out.println("error: request should not be put into service if already" +
            " in service");
        }
        inService = true;
        time = newTime;
    }

    public BigDecimal getTime () {
        return time;
    }

    public boolean inService () {
        return inService;
    }

    public double getJobSize () {
        return jobSize;
    }

    public int compareTo (Object o) {
        return ((Request) o).getTime().compareTo(time); //CHECK!!!
    }
}
