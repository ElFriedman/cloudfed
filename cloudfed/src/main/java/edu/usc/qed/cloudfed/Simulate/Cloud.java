package edu.usc.qed.cloudfed.Simulate;

import java.util.ArrayList;

public class Cloud extends ServerPool {
    public Cloud (ArrayList<Server> servers) {
        super(servers);
    }

    @Override
    public void reject (AbstractSimulator simulator, Request r) {
        rejected++;
        ((CloudSimulator)simulator).federation.insert(simulator, r);
    }
}