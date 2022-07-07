package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Departure extends Noise {
    public Server server;

    public Departure (Request r, BigDecimal time, ServerPool pool, Server server) {
        super(r, time, pool);
        this.server = server;
    }

    @Override
    public void execute (AbstractSimulator simulator) throws Exception {
        pool.listener.notify(this);
        if (pool.ID == -1) {  //if pool is the federation, then also have to notify cloud of Noise
            r.getCloud(simulator).listener.notify(this);
        }
        simulator.log(this);
    }
    public Type getType() {
        return Type.DEP;
    }
}
