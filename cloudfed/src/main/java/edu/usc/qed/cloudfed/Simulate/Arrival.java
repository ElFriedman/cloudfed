package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Arrival extends Noise {
    public Arrival (Request r, BigDecimal time, ServerPool pool) {
        super(r, time, pool);
    }

    public Type getType() {
        return Type.ARR;
    }
}
