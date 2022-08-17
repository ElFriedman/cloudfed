package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Rejection extends Noise {
    public Rejection (Request r, BigDecimal time, ServerPool pool) {
        super(r, time, pool);
    }

    public void execute (AbstractSimulator simulator) throws Exception {
        pool.listener.notify(this);
        ((CloudSimulator)simulator).federation.listener.notify(this);
        simulator.log(this);
    }

    public Type getType() {
        return Type.REJ;
    }
}
