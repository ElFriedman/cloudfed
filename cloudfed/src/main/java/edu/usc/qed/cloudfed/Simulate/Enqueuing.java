package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Enqueuing extends Noise {
    public Enqueuing (Request r, BigDecimal time) {
        super(r, time);
    }

    public Type getType() {
        return Type.ENQ;
    }
}
