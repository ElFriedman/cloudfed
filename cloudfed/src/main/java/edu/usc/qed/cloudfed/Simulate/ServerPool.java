package edu.usc.qed.cloudfed.Simulate;

import java.math.BigDecimal;
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
    public double queueNetJobSize;
    public int completed = 0;
    public int rejected = 0;
    public int rejectedOutright = 0;

    public ServerPool (ArrayList<Server> servers) {
        //check if this comparator properly overrides Comparable with time
        freeServers = new PriorityQueue<Server>((Server s1, Server s2) -> (int)(s1.workRate-s2.workRate)); 
        queue = new LinkedList<Request>();
        this.servers = servers;
        for (Server s : servers) {
            freeServers.add(s);
            s.pool = this;
        }

        //predictedDueDates = new PriorityQueue<Predictor>();
    }

    public void insert (AbstractSimulator simulator, Request r) {
        if (!freeServers.isEmpty()) {
            freeServers.poll().insert(simulator, r);
        } else {
            if (underCapacity(simulator)) {
                queue.add(r);
            } else {
                reject(simulator, r);
            }
        }
    }

    public boolean underCapacity (AbstractSimulator simulator) {
        /*
        HashMap<String, Double> streamToQoS = ((CloudSimulator) simulator).streamToQoS;
        HashMap<String, Double> streamMJS = ((CloudSimulator) simulator).streamToMJS;*/
        return false;
    }

    //can this be abstract or smth rather than this crap
    public void reject (AbstractSimulator simulator, Request r) {
        System.out.println("this should not run ServerPool reject should be overwritten");
    }
}

/*

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
*/