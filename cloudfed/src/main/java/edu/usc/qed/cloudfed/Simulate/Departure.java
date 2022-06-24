package edu.usc.qed.cloudfed.Simulate;

import java.io.IOException;
import java.math.BigDecimal;

public class Departure extends Noise {
    public Server server;

    public Departure (Request r, BigDecimal time, ServerPool pool, Server server) {
        super(r, time, pool);
        this.server = server;
    }

    @Override
    public void execute (AbstractSimulator simulator) {
        pool.listener.notify(this);
        if (pool.ID == -1) {  //if pool is the federation, then also have to notify cloud of Noise
            r.getCloud(simulator).listener.notify(this);
        }
        try {
            simulator.log(this);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Couldn't log noise");
            e.printStackTrace();
        }
    }
    public Type getType() {
        return Type.DEP;
    }
}
