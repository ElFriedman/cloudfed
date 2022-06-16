package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal; 

public class Request extends Event {
    public double jobSize;
    public String streamLabel;
    public ServerPool serverPool;
    
    public Request (String streamLabel, BigDecimal time, double jobSize, ServerPool serverPool) {
        this.time = time;
        this.jobSize = jobSize;
        this.streamLabel = streamLabel;
        this.serverPool = serverPool;
    }
    public void execute (AbstractSimulator simulator) {
        serverPool.insert(simulator, this);

    }
}