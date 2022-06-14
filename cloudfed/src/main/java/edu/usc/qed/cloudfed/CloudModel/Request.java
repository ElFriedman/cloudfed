// package edu.usc.qed.cloudfed.CloudModel;
// import java.math.BigDecimal; //add this to the cloudfed package

// public class Request implements Comparable {
//     private BigDecimal dueDate; 
//     private double jobSize; // perhaps should be BigDecimal
//     private boolean inService;
//     private boolean waitedInQueue = false;
//     // if inService is false, time is arrivalTime
//     // if inService is true, time is finishTime

//     private Server s;

//     public Request (BigDecimal arrTime, double jobSize) {
//         dueDate = arrTime;
//         this.jobSize = jobSize;
//         inService = false;
//     }

//     public void service (Server s, BigDecimal time) {
//         if (inService) {
//             System.out.println("error: request should not be put into service if already in service");
//         }
//         inService = true;

//         this.s = s;
//         s.employ(this, time);

//         BigDecimal dueDateCheck = dueDate;
//         dueDate = time.add(new BigDecimal(jobSize/s.getWorkRate()));

//         if (waitedInQueue && !dueDateCheck.equals(dueDate)) {
//             System.out.println("error: failed due date check");
//             System.out.println(dueDate);
//             System.out.println(dueDateCheck);
//         }
//     }

//     public Server deService (BigDecimal time) {
//         //maybe mark for garbage collection/dereference if necessary
//         if (!inService) { 
//             System.out.println("error: request should not be put out of service if already not in service");
//         }
//         inService = false;
//         s.fire(time);
//         return s;
//     }
 
//     public BigDecimal getDueDate () {
//         return dueDate;
//     }

//     public void setDueDate (BigDecimal dueDate) {
//         this.dueDate = dueDate;
//     }

//     public boolean inService () {
//         return inService;
//     }

//     public double getJobSize () {
//         return jobSize;
//     }

//     public int compareTo (Object o) {
//         return dueDate.compareTo(((Request)o).dueDate); //CHECK!!!
//     }

//     public String toString () {
//        return"request with:\n\tinService = " + inService + "\n\tdueDate = " + dueDate + "\n\tjob size = " + jobSize + "\n\tserver = " + s+"\n";
//     }

//     public void waitedInQueue () {
//         waitedInQueue = true;
//     }
// }
