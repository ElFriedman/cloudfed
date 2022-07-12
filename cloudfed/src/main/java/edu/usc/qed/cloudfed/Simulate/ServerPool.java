package edu.usc.qed.cloudfed.Simulate;

import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.LinkedList;
/*
 * ServerPool is the parent class to both Cloud and Federation
 */
public abstract class ServerPool {
    public ArrayList<Server> servers;
    public PriorityQueue<Server> freeServers; //priority queue of free servers, sorted by server work rate
    public Queue<Request> queue; //request queue, FIFO
    public double netWorkRate; //fixed value of server pool's net work rate calculated at construction
    public double queueNetMJS; //running sum of estimated job sizes in queue
    
    public int ID;
    public String label;

    public Listener listener;

    //basic testing - sanity check
    public int completed;
    public int overflow;
    public int rejected;

    //Constructor initializes everything
    public ServerPool (ArrayList<Server> servers, int ID, String label) {
        freeServers = new PriorityQueue<Server>((Server s1, Server s2) -> (Double.compare(s1.workRate,s2.workRate))); 
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
        completed = 0;
        overflow = 0;
        rejected = 0;
    }

    //Insert a request into the queue
    //For different queueing policies, simply add another insert method
    public void insert (AbstractSimulator simulator, Request r) throws Exception {
        if (!freeServers.isEmpty()) { //if there is an available server, serve the fastest one
            freeServers.poll().insert(simulator, r); 
        } else {
            if (underCapacity(simulator, r)) { //add to queue if under capacity
                queue.add(r);
                queueNetMJS += ((CloudSimulator) simulator).streamToMJS.get(r.streamLabel);
                Enqueuing x = new Enqueuing(r, ((Simulator)simulator).now(), this);
                x.execute(simulator);
            } else { //otherwise, reject
                reject(simulator, r);
            }
        }
    }

    //Calculate capacity based on sum of estimated job sizes in queue
    public boolean underCapacity (AbstractSimulator simulator, Request r) {        
        return queueNetMJS/netWorkRate <= ((CloudSimulator) simulator).streamToQoS.get(r.streamLabel);
    }

    //reject is implemented by Cloud and Federation
    public abstract void reject (AbstractSimulator simulator, Request r) throws Exception;
}