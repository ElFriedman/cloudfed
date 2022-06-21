package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Departure extends Noise {
    public Departure (Request r, BigDecimal time) {
        super(r, time);
    }

    public Type getType() {
        return Type.DEP;
    }
}
