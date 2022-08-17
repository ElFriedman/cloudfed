package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;
/*
 * Noises are events that contain information on the movement of requests
 * Subclasses: arrival, departure, rejection, enqueuing, servicing, overflow
 */
public abstract class Noise extends Event {
    public Request r;
    public ServerPool pool;

    public enum Type {ARR, DEP, REJ, ENQ, SER, OVR};
    
    public Noise (Request r, BigDecimal time, ServerPool pool) {
        this.r = r;
        this.time = time;
        this.pool = pool;
    }
    
    public void execute (AbstractSimulator simulator) throws Exception {
        pool.listener.notify(this);
        simulator.log(this);
    }

    public abstract Type getType();
}
