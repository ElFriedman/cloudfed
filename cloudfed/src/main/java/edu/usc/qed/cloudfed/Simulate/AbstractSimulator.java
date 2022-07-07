package edu.usc.qed.cloudfed.Simulate;

import java.io.IOException;
import java.util.PriorityQueue;

public abstract class AbstractSimulator {
    PriorityQueue<AbstractEvent> events;
    
    public void insert(AbstractEvent e) {
        events.add(e);
    }

    public abstract void log (AbstractEvent e) throws IOException;
 }