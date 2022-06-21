package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public abstract class Noise extends Event {
    public Request r;

    public enum Type {ARR, DEP, REJ, ENQ, DEQ, SER};
    
    public Noise (Request r, BigDecimal time) {
        this.r = r;
        this.time = time;
    }
    
    public void execute (AbstractSimulator simulator) {
        r.getCloud(simulator).listener.notify(this);
    }

    public abstract Type getType();
}
