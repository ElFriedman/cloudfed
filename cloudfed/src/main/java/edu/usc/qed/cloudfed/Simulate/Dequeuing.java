package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Dequeuing extends Noise {
    public Dequeuing (Request r, BigDecimal time) {
        super(r, time);
    }

    public Type getType() {
        return Type.DEQ;
    }
}
