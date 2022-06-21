package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Service extends Noise {
    public Service (Request r, BigDecimal time) {
        super(r, time);
    }

    public Type getType() {
        return Type.SER;
    }
}
