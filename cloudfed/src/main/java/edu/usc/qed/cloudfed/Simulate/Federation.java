package edu.usc.qed.cloudfed.Simulate;

import java.util.ArrayList;

public class Federation extends ServerPool {
    public Federation (ArrayList<Server> servers) {
        super(servers);
    }

    @Override
    public void reject (AbstractSimulator simulator, Request r) {
        //do smth. notify da peeps
        rejected++;
        r.getCloud(simulator).rejectedOutright++;
    }
}