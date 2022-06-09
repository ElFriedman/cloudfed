package edu.usc.qed.cloudfed.CloudModel;

import java.util.PriorityQueue;

public class Cloud {
    private WorkloadGenerator wGenerator;
    private PriorityQueue<Request> eventQueue;
    private Queue<Request> serverQueue; 
    //currently the queue could be under capacity but the expected wait time of the newest 
    //request would be over the QoS requirement, however it would not be rejected

    //should servers be baked into cloud or be baked into a server pool, which is then
    //baked with workload generator into a cloud, allowing the federation cloud to act as a 
    //server pool with the same class structure? probably

    public Cloud (double lambda, double jobSizeMin, double jobSizeMax, double QoS,
    double servers, int arrivalProcessType, int jobGeneratorType) {
        ArrivalProcess arrProcess = null;
        JobGenerator jGenerator = null;

        if (arrivalProcessType == 0) {
            arrProcess = new PoissonArrivalProcess(lambda);
        }
        if (jobGeneratorType == 0) {
            jGenerator = new UniformJobGenerator(jobSizeMin, jobSizeMax);
        }
        wGenerator = new WorkloadGenerator(arrProcess, jGenerator);
        eventQueue = new PriorityQueue<Request>();
        serverQueue = new Queue<Request>();
    }

    public void run (double endTime) {
        double time = 0;
        eventQueue.add(wGenerator.generateRequest(0));
        while (!eventQueue.isEmpty() && time < endTime) {
            Request r = eventQueue.remove();
            if (r.inService()) {
                
            } else {
                if (getCapacity()) {

                }
            }
        }
    }

    public double getCapacity () {
        return 0;
    }



}