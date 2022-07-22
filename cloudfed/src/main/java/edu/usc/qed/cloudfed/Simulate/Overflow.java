package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Overflow extends Noise {
    public Overflow (Request r, BigDecimal time, ServerPool pool) {
        super(r, time, pool);
    }

    public Type getType() {
        return Type.OVR;
    }
}
