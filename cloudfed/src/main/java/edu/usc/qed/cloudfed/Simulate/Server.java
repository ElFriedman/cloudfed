package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Server extends Event {
    public double workRate;
    
    public Request r;
    public ServerPool pool;

    public Server (double workRate) {
        this.workRate = workRate;
        r = null;
    }

    //finish task
    public void execute (AbstractSimulator simulator) {
        simulator.insert(new Departure(r, ((Simulator)simulator).now()));
        pool.completed++;

        r = null;
        if (!pool.queue.isEmpty()) {
            Request req = pool.queue.poll();
            insert(simulator, req);
            simulator.insert(new Dequeuing(req, ((Simulator)simulator).now()));
            pool.queueNetMJS -= ((CloudSimulator) simulator).streamToMJS.get(req.streamLabel);
        } else {
            pool.freeServers.add(this);
        }
    }

    public void insert (AbstractSimulator simulator, Request req) {
        if (r != null) {
            System.out.println("Error: server is busy with another request");
        }
        r = req;
        double serviceTime = r.jobSize/workRate;
        time = ((Simulator) simulator).now().add(new BigDecimal(serviceTime));
        simulator.insert(this);
    }
}