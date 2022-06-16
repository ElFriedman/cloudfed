package edu.usc.qed.cloudfed.Simulate;

import java.util.PriorityQueue;

public class AbstractSimulator {
    PriorityQueue<AbstractEvent> events;
    
    public void insert(AbstractEvent e) {
        events.add(e);
    }
 }