package edu.usc.qed.cloudfed.Simulate;

import java.io.IOException;
import java.math.BigDecimal;

public abstract class Noise extends Event {
    public Request r;
    public ServerPool pool;

    public enum Type {ARR, DEP, REJ, ENQ, SER};
    
    public Noise (Request r, BigDecimal time, ServerPool pool) {
        this.r = r;
        this.time = time;
        this.pool = pool;
    }
    
    public void execute (AbstractSimulator simulator) {
        pool.listener.notify(this);
        try {
            simulator.log(this);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Couldn't log noise");
            e.printStackTrace();
        }
    }

    public abstract Type getType();
}
