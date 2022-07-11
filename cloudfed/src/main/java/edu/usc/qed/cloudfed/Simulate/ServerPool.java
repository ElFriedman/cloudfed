package edu.usc.qed.cloudfed.Simulate;

import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.LinkedList;

public abstract class ServerPool {
    public ArrayList<Server> servers; //might be unnecessary - check back later
    public PriorityQueue<Server> freeServers;
    public Queue<Request> queue;
    public double netWorkRate;
    public double queueNetMJS;
    
    public int ID;
    public String label;

    public Listener listener;

    //basic testing - sanity check
    public int completed = 0;
    public int overflow = 0;
    public int rejected = 0;

    public ServerPool (ArrayList<Server> servers, int ID, String label) {
        //check if this comparator properly overrides Comparable with time
        freeServers = new PriorityQueue<Server>((Server s1, Server s2) -> (int)(s1.workRate-s2.workRate)); 
        queue = new LinkedList<Request>();
        this.servers = servers;
        for (Server s : servers) {
            netWorkRate += s.workRate;
            freeServers.add(s);
            s.pool = this;
        }
        queueNetMJS = 0;

        this.ID = ID;
        this.label = label;
        listener = new Listener();
    }

    public void insert (AbstractSimulator simulator, Request r) throws Exception {
        if (!freeServers.isEmpty()) {
            freeServers.poll().insert(simulator, r);
        } else {
            if (underCapacity(simulator, r)) {
                queue.add(r);
                queueNetMJS += ((CloudSimulator) simulator).streamToMJS.get(r.streamLabel);
                Enqueuing x = new Enqueuing(r, ((Simulator)simulator).now(), this);
                x.execute(simulator);
            } else {
                reject(simulator, r);
            }
        }
    }

    public boolean underCapacity (AbstractSimulator simulator, Request r) {        
        return queueNetMJS/netWorkRate <= ((CloudSimulator) simulator).streamToQoS.get(r.streamLabel);
    }

    public abstract void reject (AbstractSimulator simulator, Request r) throws Exception;
}