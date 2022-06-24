package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;

public class Server extends Event {
    public double workRate;
    public int ID;
    public Request r;
    public ServerPool pool;

    public Server (double workRate, int ID) {
        this.workRate = workRate;
        r = null;
        this.ID = ID;
    }

    //finish task
    public void execute (AbstractSimulator simulator) {
        simulator.insert(new Departure(r, ((Simulator)simulator).now(), pool, this));
        pool.completed++;

        r = null;
        if (!pool.queue.isEmpty()) {
            Request req = pool.queue.poll();
            insert(simulator, req);
            pool.queueNetMJS -= ((CloudSimulator) simulator).streamToMJS.get(req.streamLabel);
        } else {
            pool.freeServers.add(this);
        }
    }

    public void insert (AbstractSimulator simulator, Request r) {
        if (this.r != null) {
            System.out.println("Error: server is busy with another request");
        }
        this.r = r;
        simulator.insert(new Servicing(r, ((Simulator)simulator).now(), pool, this));
        double serviceTime = r.jobSize/workRate;
        time = ((Simulator) simulator).now().add(new BigDecimal(serviceTime));
        simulator.insert(this);
    }
}