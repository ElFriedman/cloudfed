package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal; 

public class Request extends Event {
    public double jobSize;
    public String streamLabel;
    public int ID;
    
    public Request (String streamLabel, BigDecimal time, double jobSize, int ID) {
        this.time = time;
        this.jobSize = jobSize;
        this.streamLabel = streamLabel;
        this.ID = ID;
    }

    public void execute (AbstractSimulator simulator) throws Exception {
        throw new Exception("Wee woo wee woo why is a request getting executed");
    }

    public Cloud getCloud(AbstractSimulator simulator) {
        return ((CloudSimulator)simulator).streamToCloud.get(streamLabel);
    }
}