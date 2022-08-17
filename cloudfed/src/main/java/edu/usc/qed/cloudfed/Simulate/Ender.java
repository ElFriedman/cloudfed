package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Ender extends Event {
    public Ender (BigDecimal time) {
        this.time = time.add(new BigDecimal(0.000000001));
    }

    public void execute (AbstractSimulator simulator) throws Exception {
        throw new Exception ("Ender");
    }

}
