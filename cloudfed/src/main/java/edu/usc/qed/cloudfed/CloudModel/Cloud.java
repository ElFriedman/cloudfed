package edu.usc.qed.cloudfed.CloudModel;

import java.util.PriorityQueue;
import java.math.BigDecimal;

public class Cloud {
    private WorkloadGenerator wGenerator;
    private PriorityQueue<Request> eventQueue;
    private ServerPool serverPool;
    //currently the queue could be under capacity but the expected wait time of the newest 
    //request would be over the QoS requirement, however it would not be rejected

    //should servers be baked into cloud or be baked into a server pool, which is then
    //baked with workload generator into a cloud, allowing the federation cloud to act as a 
    //server pool with the same class structure? probably

    //eventually, ServerPool should be an interface, with this constructor determining if 
    //HomogenousServerPool or another variety is chosen
    public Cloud (double lambda, double jobSizeMin, double jobSizeMax, double QoS,
    double workRate, int serverCount, int arrivalProcessType, int jobGeneratorType) {
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
        serverPool = new ServerPool(workRate, serverCount);
    }

    public void run (BigDecimal endTime) {
        BigDecimal time = new BigDecimal(0);
        eventQueue.add(wGenerator.generateRequest(time));
        while (!eventQueue.isEmpty() && endTime.compareTo(time)>0) { // check >/<
            Request r = eventQueue.remove();
            if (r.inService()) {
                
            } else {
                
            }
        }
    }

    



}