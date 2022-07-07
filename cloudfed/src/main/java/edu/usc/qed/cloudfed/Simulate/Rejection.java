package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Rejection extends Noise {
    public Rejection (Request r, BigDecimal time, ServerPool pool) {
        super(r, time, pool);
    }

    public Type getType() {
        return Type.REJ;
    }
}
