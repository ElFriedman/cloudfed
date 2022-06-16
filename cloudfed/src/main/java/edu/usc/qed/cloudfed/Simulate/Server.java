package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Server extends Event {
    public double workRate;
    
    public Request r;
    public ServerPool pool;

    public Server (double workRate) {
        this.workRate = workRate;
    }

    //can delete this?
    public boolean isAvailable() {
        return r == null;
    }

    public void execute (AbstractSimulator simulator) {
        //notify listeners with time and request?
        r = null;
        if (!pool.queue.isEmpty()) {
            r = pool.queue.poll();
            insert(simulator, r);
        } else {
            pool.freeServers.add(this);
        }
    }

    public void insert (AbstractSimulator simulator, Request r) {
        if (r != null) {
            System.out.println("Error: server is busy with another request");
        }
        this.r = r;
        double serviceTime = r.jobSize/workRate;
        time = ((Simulator) simulator).now().add(new BigDecimal(serviceTime));
        simulator.insert(this);
        /*
        double MJS = ((CloudSimulator)simulator).streamToMJS.get(r.streamLabel);
        pool.predictedDueDates.add(new Predictor(((CloudSimulator)simulator).now().add(new BigDecimal(r.jobSize/MJS))));*/
    }
}