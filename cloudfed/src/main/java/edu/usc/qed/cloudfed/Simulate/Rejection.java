package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Rejection extends Noise {
    public Rejection (Request r, BigDecimal time) {
        super(r, time);
    }

    public Type getType() {
        return Type.REJ;
    }
}
