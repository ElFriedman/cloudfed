package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Predictor extends Event {
    public Server s;
    
    public Predictor (BigDecimal time) {
        this.time = time;
    }

    public void execute (AbstractSimulator simulator) {

    }
}
