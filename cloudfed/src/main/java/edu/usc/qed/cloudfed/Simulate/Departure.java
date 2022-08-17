package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Departure extends Noise {
    public Server server;

    public Departure (Request r, BigDecimal time, ServerPool pool) {
        super(r, time, pool);
    }

    public Type getType() {
        return Type.DEP;
    }
}
