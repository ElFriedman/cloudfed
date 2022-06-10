package edu.usc.qed.cloudfed.CloudModel;

import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

public class ServerPool {
    private ArrayList<Server> servers;
    private Queue<Request> requestQueue; 


    //constructor for homogenous work rate
    public ServerPool (double workRate, int serverCount) {
        servers = new ArrayList<Server>();
        for (int i = 0; i < serverCount; i++) {
            servers.add(new Server(workRate));
        }
        requestQueue = new LinkedList<Request>();
    }

    public boolean putRequest (Request r) {
        if ()
    }
    
    public double getCapacity () {
        return 0;
    }
}
