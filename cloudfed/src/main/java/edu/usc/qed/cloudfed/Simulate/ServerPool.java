package edu.usc.qed.cloudfed.Simulate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.LinkedList;

public class ServerPool {
    public ArrayList<Server> servers; //might be unnecessary - check back later
    public PriorityQueue<Server> freeServers;
    public Queue<Request> queue;
    public double netWorkRate;
    public double queueNetMJS;

    //basic testing
    public int completed = 0;
    public int rejected = 0;
    public int rejectedOutright = 0;

    public ServerPool (ArrayList<Server> servers) {
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

    }

    public void insert (AbstractSimulator simulator, Request r) {
        if (!freeServers.isEmpty()) {
            freeServers.poll().insert(simulator, r);
        } else {
            if (underCapacity(simulator, r)) {
                queue.add(r);
                queueNetMJS += ((CloudSimulator) simulator).streamToMJS.get(r.streamLabel);
            } else {
                reject(simulator, r);
            }
        }
    }

    public boolean underCapacity (AbstractSimulator simulator, Request r) {        
        return queueNetMJS/(netWorkRate) <= ((CloudSimulator) simulator).streamToQoS.get(r.streamLabel);
    }

    //can this be abstract or smth rather than this crap
    public void reject (AbstractSimulator simulator, Request r) {
        System.out.println("this should not run ServerPool reject should be overwritten");
    }
}