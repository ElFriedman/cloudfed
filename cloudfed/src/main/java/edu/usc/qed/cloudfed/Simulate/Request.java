package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal; 

public class Request extends Event {
    public double jobSize;
    public String streamLabel;
    
    public Request (String streamLabel, BigDecimal time, double jobSize) {
        this.time = time;
        this.jobSize = jobSize;
        this.streamLabel = streamLabel;
    }

    public void execute (AbstractSimulator simulator) {
        System.out.println("Wee woo wee woo why are is this running");
        getCloud(simulator).insert(simulator, this);
    }

    public Cloud getCloud(AbstractSimulator simulator) {
        return ((CloudSimulator)simulator).streamToCloud.get(streamLabel);
    }
}