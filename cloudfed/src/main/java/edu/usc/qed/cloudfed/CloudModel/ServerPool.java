package edu.usc.qed.cloudfed.CloudModel;

import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.math.BigDecimal;

public class ServerPool {
    private ArrayList<Server> servers;
    private Queue<Server> freeServers;
    private Queue<Request> requestQueue;

    private double workRate;


    private PriorityQueue<Request> predictedDueDates;


    //constructor for homogenous work rate
    public ServerPool (double workRate, int serverCount) {
        servers = new ArrayList<Server>();
        freeServers = new LinkedList<Server>();
        if (workRate <= 0) {
            System.out.println("Work rate must > 0");
        }
        servers = new ArrayList<Server>();
        for (int i = 0; i < serverCount; i++) {
            servers.add(new Server(workRate));
            freeServers.add(servers.get(i));
        }
        requestQueue = new LinkedList<Request>();
        this.workRate = workRate;

        predictedDueDates = new PriorityQueue<Request>();
    }

    public Request putRequest (Request r, BigDecimal time) {
        if (freeServers.isEmpty()) {
            requestQueue.add(r);

            BigDecimal newDueDate = predictedDueDates.poll().getDueDate().add(new BigDecimal(r.getJobSize()/workRate));
            r.setDueDate(newDueDate);
            r.waitedInQueue();
            predictedDueDates.add(r);

            return null;
        } else {
            Server s = freeServers.poll();
            r.service(s, time);
            
            predictedDueDates.add(r);

            return r;
        }
    }

    //omniscient
    public boolean underCapacity(double QoS, BigDecimal time) {
        if (!freeServers.isEmpty()) {
            return true;
        }
        if (predictedDueDates.peek().getDueDate().compareTo(new BigDecimal(QoS))>0) {
            return true;
        }
        return false;

    }
    

    public Request finishRequest (Request r, BigDecimal time) {
        Server s = r.deService(time);
        if (requestQueue.isEmpty()) {
            freeServers.add(s);
            return null;
        } else {
            r = requestQueue.poll();
            r.service(s, time);
            return r;
        }
    }
    public ArrayList<Server> getServers() {
        return servers;
    }

    public int serversInUse() {
        return servers.size()-freeServers.size();
    }
}
