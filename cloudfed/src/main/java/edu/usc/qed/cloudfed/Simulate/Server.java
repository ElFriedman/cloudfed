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
    public void execute (AbstractSimulator simulator) throws Exception {
        Departure x = new Departure(r, ((Simulator)simulator).now(), pool);
        x.execute(simulator);

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

    public void insert (AbstractSimulator simulator, Request r) throws Exception {
        if (this.r != null) {
            throw new Exception("Error: server is busy with another request");
        }
        this.r = r;
        Servicing x = new Servicing(r, ((Simulator)simulator).now(), pool, this);
        x.execute(simulator);
        double serviceTime = r.jobSize/workRate;
        time = ((Simulator) simulator).now().add(new BigDecimal(serviceTime));
        simulator.insert(this);
    }
}