package edu.usc.qed.cloudfed.Simulate;

import java.util.ArrayList;

public class Cloud extends ServerPool {
    public String label;

    public Cloud (ArrayList<Server> servers, int ID) {
        super(servers, ID, "cloud"+ID);
    }

    @Override
    /* 
     * Forwards the request to the federation pool, and executes a rejection noise
     */
    public void reject (AbstractSimulator simulator, Request r) throws Exception {
        overflow++;
        ((CloudSimulator)simulator).federation.insert(simulator, r);
        Rejection x = new Rejection(r, ((Simulator)simulator).now(), this);
        x.execute(simulator);
    }
}