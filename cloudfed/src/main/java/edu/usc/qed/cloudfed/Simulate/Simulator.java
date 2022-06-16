package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public abstract class Simulator extends AbstractSimulator {
    public BigDecimal time;
    
    public BigDecimal now() {
        return time;
    }

    public void doAllEvents () {
        Event e;
        while ((e = (Event) events.poll()) != null) {
            time = e.time;
            e.execute(this);
        }
    }
}
