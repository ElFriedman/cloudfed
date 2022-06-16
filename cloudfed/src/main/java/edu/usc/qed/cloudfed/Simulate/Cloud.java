package edu.usc.qed.cloudfed.Simulate;

import java.util.ArrayList;

public class Cloud extends ServerPool {
    public Cloud (ArrayList<Server> servers) {
        super(servers);
    }

    @Override
    public void reject (AbstractSimulator simulator, Request r) {
        ((CloudSimulator)simulator).federation.insert(simulator, r);
    }
}
//     private WorkloadGenerator wGenerator;
//     private PriorityQueue<Request> eventQueue;
//     private double QoS;

//     private int requestsServiced;
//     private int requestsRejected;
//     private int totalRequests;

//     //currently the queue could be under capacity but the expected wait time of the newest 
//     //request would be over the QoS requirement, however it would not be rejected

//     //should servers be baked into cloud or be baked into a server pool, which is then
//     //baked with workload generator into a cloud, allowing the federation cloud to act as a 
//     //server pool with the same class structure? probably

//     //eventually, ServerPool should be an interface, with this constructor determining if 
//     //HomogenousServerPool or another variety is chosen
//     public Cloud (double lambda, double jobSizeMin, double jobSizeMax, double QoS,
//     double workRate, int serverCount, int arrivalProcessType, int jobGeneratorType) {
//         ArrivalProcess arrProcess = null;
//         JobGenerator jGenerator = null;
//         if (arrivalProcessType == 0) {
//             arrProcess = new PoissonArrivalProcess(lambda);
//         }
//         if (jobGeneratorType == 0) {
//             jGenerator = new UniformJobGenerator(jobSizeMin, jobSizeMax);
//         }
//         wGenerator = new WorkloadGenerator(arrProcess, jGenerator);
//         eventQueue = new PriorityQueue<Request>();
//         serverPool = new ServerPool(workRate, serverCount);
//         this.QoS = QoS;
        
//         requestsServiced = 0;
//         requestsRejected = 0;
//         totalRequests = 0;
//     }

//     public void run (BigDecimal endTime) {
//         BigDecimal time = new BigDecimal(0);
//         eventQueue.add(wGenerator.generateRequest(time));
//         while (endTime.compareTo(time) > 0 ) {
//             Request r = eventQueue.remove();            
//             time = new BigDecimal(r.getDueDate().toString());
//             if (r.inService()) { //if service of this request just finished
//                 Request req = serverPool.finishRequest(r, time);
//                 if (req != null) {
//                     eventQueue.add(req);
//                 }
//                 requestsServiced += 1;
//             } else { //if this request just arrived
//                 totalRequests += 1;
//                 if (serverPool.underCapacity(QoS, time)) {
//                     Request req = serverPool.putRequest(r, time);
//                     if (req != null) {
//                         eventQueue.add(req);
//                     }
//                 } else {
//                     rejectRequest(r);
//                 }
//                 eventQueue.add(wGenerator.generateRequest(time));
//             }
//             /* 
//             System.out.println("time = " + time);
//             System.out.println(eventQueue);
//             System.out.println();
//             */
//         }
//         System.out.println("After time " + time);
//         System.out.println(requestsRejected + " rejected requests");
//         System.out.println(requestsServiced + " serviced requests");
//         System.out.println(serverPool.serversInUse() + " servers being served");
//         System.out.println((requestsRejected + requestsServiced + serverPool.serversInUse()) + " combined requests");
//         System.out.println(totalRequests + " total requests (should be same as ^)");
//         System.out.println("rejection rate: " + (double)requestsRejected/(double)totalRequests);
    
//         displayUptimes(time);
//     }

//     public void rejectRequest(Request r) {
//         requestsRejected += 1;
//     }

//     //note uptimes are being converted to doubles. probably ok but double check
//     public boolean crosscheckUptimes(BigDecimal time) {
//         for (Server s : serverPool.getServers()) {
//             if(!s.crosscheckUptimes(time)) {
//                 System.out.println("up/up+down: " + s.uptime1());
//                 System.out.println("up/time: " + s.uptime2(time));
//                 return false;
//             }
//         }
//         return true;
//     }
    
//     //note uptimes are being converted to doubles. probably ok but double check
//     public void displayUptimes (BigDecimal time) { 
//         if (!crosscheckUptimes(time)) {
//             System.out.println("***Uptimes failed crosscheck test***");
//         } else {
//             System.out.println("Uptimes passed crosscheck test");
//         }
//         double totalUptime = 0;
//         int n = 0;
//         System.out.println("Server Uptimes");
//         for (Server s : serverPool.getServers()) {
//             System.out.println("\t"+n+": "+s.uptime1());
//             totalUptime += s.uptime1();
//             n += 1;
//         }
//         System.out.println("Mean uptime: " + totalUptime/n);
//     }

//     public static void main (String [] args ) {
//         Cloud cloud9 = new Cloud(2, 3, 4, 8, 1, 7, 0, 0);
//         cloud9.run(new BigDecimal(100000));
//     }
// }