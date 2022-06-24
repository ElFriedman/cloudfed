package edu.usc.qed.cloudfed.Simulate;

import java.util.ArrayList;

public class Federation extends ServerPool {
    public Federation (ArrayList<Server> servers) {
        super(servers, -1, "Fed");
    }

    @Override
    public void reject (AbstractSimulator simulator, Request r) {
        //relpace this
        rejected++;
        r.getCloud(simulator).rejectedOutright++;
        simulator.insert(new Rejection(r, ((Simulator)simulator).now(), this));
    }
}