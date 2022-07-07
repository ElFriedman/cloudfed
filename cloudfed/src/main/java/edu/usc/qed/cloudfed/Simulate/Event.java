package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public abstract class Event implements AbstractEvent {
    public BigDecimal time;

    public int compareTo(Object o) {
        return time.compareTo(((Event)o).time);
    }
}
