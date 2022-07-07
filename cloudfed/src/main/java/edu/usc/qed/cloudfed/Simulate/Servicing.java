package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Servicing extends Noise {
    public Server server;

    public Servicing (Request r, BigDecimal time, ServerPool pool, Server server) {
        super(r, time, pool);
        this.server = server;
    }

    public Type getType() {
        return Type.SER;
    }
}
