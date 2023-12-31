package edu.usc.qed.cloudfed.Simulate;

import java.util.ArrayList;

public class Federation extends ServerPool {
    public Federation (ArrayList<Server> servers) {
        super(servers, -1, "Fed");
    }

    @Override
    public void reject (AbstractSimulator simulator, Request r) throws Exception {
        overflow++; //for federation, overflow = rejected
        r.getCloud(simulator).rejected++;
        Rejection x = new Rejection(r, ((Simulator)simulator).now(), r.getCloud(simulator));
        x.execute(simulator);
    }
}