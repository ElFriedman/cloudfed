package edu.usc.qed.cloudfed;

import java.util.concurrent.Callable;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.qed.cloudfed.Workload.*;
import edu.usc.qed.cloudfed.Simulate.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.regex.Pattern;

import javax.swing.WindowConstants;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ScopeType;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.jfree.chart.ChartUtils;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.stat.inference.TTest;

import com.esotericsoftware.yamlbeans.YamlReader;

/*
 * https://github.com/EsotericSoftware/yamlbeans
 * https://github.com/msgpack/msgpack-java/blob/develop/msgpack-core/src/test/java/org/msgpack/core/example/MessagePackExample.java
 * https://www.baeldung.com/java-picocli-create-command-line-program
 * 
 */

@Command(name = "cloudfed", mixinStandardHelpOptions = true, version = "cloudfed 1.0",
         description = "Cloud Federation Simulator",
         scope = ScopeType.INHERIT,
         subcommands = { /*Main.Seed.class,*/ Main.Workload.class, Main.Simulate.class, Main.Metrics.class, Main.Experiment.class })
public class Main {
    private final static Logger logger =  LoggerFactory.getLogger(Main.class); //currently unused
    private static long seed = 0; //default seed
    private static RandomGeneratorFactory<RandomGenerator> RGF = RandomGeneratorFactory.of("Random");
    private static RandomGenerator rng;
    private static ArrayList<Integer[]> basics;
    private static ArrayList<Integer[]> listeners;
    private static ArrayList<Integer[]> metrics;

    private static TimeSeriesChart chart;
    //overflow, rejected, completed locally

    @Option(names = "--verbose", defaultValue = "false", scope = ScopeType.INHERIT,
            description = "Log additional debugging information (default: ${DEFAULT-VALUE})")
    private boolean verbose;

   /*@Command(name = "seed", description = "Set the seed for the simulation")
    static class Seed implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Parameters (index = "0") private String seedString;

        @Override public Integer call() throws Exception {
            System.out.println("Setting the seed");
            System.out.println(seedString);
            seed = Long.parseLong(seedString);
            rng = RGF.create(seed);
            return 0;
        }
    }*/

    @Command(name = "workload", description = "Generate, analyze, or merge workloads.", subcommands = {Generate.class, Info.class, Merge.class})
    static class Workload implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Override public Integer call() throws Exception {
            System.out.println("Thinking about a workload");

            //should provide info on what subcommands to enter
            //maybe smth like workload help

            return 0;
        }
    }

    @Command(name = "generate", description = "Generate a workload.")
    static class Generate implements Callable<Integer> {
        @ParentCommand private Workload parentWorkload;

        @Parameters(index = "0") private String fileName;

        @Option(names = {"-S", "--seed"}, description = "Set a seed") private String seedString;

        @Parameters(index = "1") private String stopCritString;
        @Parameters(index = "2..*") private ArrayList<String> streamStrings;
        
        @Override public Integer call() throws Exception {
            System.out.println("Genenerating a workload");

            //setting random numbger generator seed
            if (seedString != null) {
                seed = Long.parseLong(seedString);
                rng = RGF.create(seed);
            } else { //if no seed given, then make a random long for the seed
                seed = Long.parseLong("" + ((int)(1000000*Math.random())));
                rng = RGF.create(seed);
            }

            //stopping criteria
            StoppingCriterion stoppingCriterion = null;
            if (Pattern.matches("Job\\[\\d+\\]", stopCritString)) { //job based criterion
                stoppingCriterion = new JobStoppingCriterion(Integer.parseInt(stopCritString.substring(4, stopCritString.length()-1)));
            } else if (Pattern.matches("Time\\[\\d+(\\.\\d+)?\\]", stopCritString)) { //time based criterion
                stoppingCriterion = new TimeStoppingCriterion(new BigDecimal(stopCritString.substring(5, stopCritString.length()-1)));
            } else {
                System.out.println("Invalid stopping criterion");
            }

            ArrayList<WorkloadStream> streams = new ArrayList<WorkloadStream>();
            HashMap<String, Double> streamToMJS = new HashMap<String, Double>();
            HashMap<String, Double> streamToQoS = new HashMap<String, Double>();

            for (String s : streamStrings) {
                //streamLabel
                int colon0 = s.indexOf(":");
                String streamLabel = s.substring(0, colon0);
                s = s.substring(colon0 + 1);

                //QoS
                int colon00 = s.indexOf(":");
                double QoS = Double.parseDouble(s.substring(0, colon00));
                s = s.substring(colon00 + 1);


                //ArrivalProcess
                ArrivalProcess arrivalProcess = null;
                int colon1 = s.indexOf("]") + 1;
                String APString = s.substring(0, colon1);
                if(Pattern.matches("Exp\\[\\d+(\\.\\d+)?\\]", APString)) { //Poisson
                    arrivalProcess = new PoissonArrivalProcess(rng, Double.parseDouble(APString.substring(4, colon1 - 1)));
                } else if(Pattern.matches("Reg\\[\\d+(\\.\\d+)?\\]", APString)) { //Periodic
                    arrivalProcess = new PeriodicArrivalProcess(Double.parseDouble(APString.substring(4, colon1 - 1)));
                } else {
                    System.out.println("Invalid arrival process distribution");
                }
                
                //BatchSizer
                BatchSizer batchSizer = null;
                String s2 = s.substring(colon1 + 1);
                int colon2 = s2.indexOf("]") + 1;
                String BSString = s2.substring(0, colon2);
                if (Pattern.matches("Det\\[\\d+\\]", BSString)) { //Deterministic
                    batchSizer = new DetBatchSizer(Integer.parseInt(BSString.substring(4, colon2 - 1)));
                } else if (Pattern.matches("Dist\\[0\\.\\d+:\\d+(\\.\\d+)?(,0\\.\\d+:\\d+(\\.\\d+)?)*\\]", BSString)) { //Distributed
                    int entries = 1 + (int) (BSString.chars().filter(ch -> ch == ',').count());
                    double[] probabilities = new double[entries];
                    int[] batchSizes = new int[entries];
                    BSString = BSString.substring(5);
                    int stop = 0;
                    for (int i = 0; i < entries; i++) {
                        stop = BSString.indexOf(":");
                        probabilities[i] = Double.parseDouble(BSString.substring(0, stop));
                        BSString = BSString.substring(stop + 1);
                        stop = BSString.indexOf(",") < 0 ? BSString.indexOf("]") : BSString.indexOf(",");
                        batchSizes[i] = Integer.parseInt(BSString.substring(0, stop));
                        BSString = BSString.substring(stop + 1);
                    }
                    batchSizer = new DistBatchSizer(rng, probabilities, batchSizes);
                } else {
                    System.out.println("Invalid batch sizer distribution");
                }

                //JobGenerator
                JobGenerator jobGenerator = null;
                String s3 = s2.substring(colon2 + 1);
                if (Pattern.matches("Unif\\[\\d+(\\.\\d+)?,\\d+(\\.\\d+)?\\]", s3)) { //Uniform
                    jobGenerator = new UniformJobGenerator(rng, Double.parseDouble(s3.substring(5, s3.indexOf(","))), 
                    Double.parseDouble(s3.substring(s3.indexOf(",") + 1, s3.length() - 1)));
                } else if (Pattern.matches("Par\\[\\d+(\\.\\d+)?,\\d+(\\.\\d+)?\\]", s3)) { //Pareto
                    jobGenerator = new ParetoJobGenerator(rng, Double.parseDouble(s3.substring(4, s3.indexOf(","))),
                    Double.parseDouble(s3.substring(s3.indexOf(",") + 1, s3.length() - 1)));
                } else if (Pattern.matches("Exp\\[\\d+(\\.\\d+)?\\]", s3)) { //Exponential
                    jobGenerator = new ExponentialJobGenerator(rng, Double.parseDouble(s3.substring(4, s3.length()-1)));
                }
                else {
                    System.out.println("Invalid job generator distribution");
                }

                streamToMJS.put(streamLabel, jobGenerator.meanJobSize());
                streamToQoS.put(streamLabel, QoS);

                streams.add(new WorkloadStream(arrivalProcess, batchSizer, jobGenerator, streamLabel));
            }
            WorkloadGenerator generator = new WorkloadGenerator(streams, new BigDecimal(0));

            MessagePacker packer = MessagePack.newDefaultPacker(new FileOutputStream(fileName));
            if(streamToMJS.size() != streamToQoS.size()) {
                throw new Exception ("Error: size of streamToQoS != size of streamToMJS");
            }
            packer.packInt(streamToMJS.size());
            for (String streamLabel : streamToMJS.keySet()) {
                packer.packString(streamLabel);
                packer.packDouble(streamToMJS.get(streamLabel));
                packer.packDouble(streamToQoS.get(streamLabel));
            }
            generator.generateWorkload(packer, stoppingCriterion);
            packer.close();
            System.out.println("Workload generated");
            return 0; //should i use this return for anything
        }
    }
    @Command(name = "info", description = "Analyze a workload.")
    static class Info implements Callable<Integer> {
        @ParentCommand private Workload parentWorkload;
        
        @Option(names = {"-A", "--all"}) private boolean allStreams;

        @Parameters(index = "0") private String fileName;
        @Parameters(index = "1..*") private ArrayList<String> streams;
    
        @Override public Integer call() throws Exception {
            System.out.println("Analyzing a workload");
            if (!allStreams && streams == null) {
                System.out.println("Error: put -A or list which streams you wish to analyze");
            }
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(fileName));
            int n = unpacker.unpackInt();
            HashMap<String, Boolean> streamBool = new HashMap<String, Boolean>();
            for (int i = 0; i < n; i++) {
                String streamLabel = unpacker.unpackString();
                streamBool.put(streamLabel, allStreams || streams.contains(streamLabel));
                String out = streamLabel + ": mean job size = " + unpacker.unpackDouble() + ", QoS requirement = " + unpacker.unpackDouble();
                if (streamBool.get(streamLabel)) {
                    System.out.println(out);
                }
            }
            int jobCount = 0;
            int batchCount = 0;
            BigDecimal oldT = new BigDecimal(-1);
            BigDecimal newT = new BigDecimal(-1);
            double netJobSize = 0;
            while(unpacker.hasNext()) {
                String streamLabel = unpacker.unpackString();
                if (streamBool.get(streamLabel)) {
                    oldT = newT;
                    newT = new BigDecimal(unpacker.unpackString());
                    if (!newT.equals(oldT)) {
                        batchCount++;
                     }
                    double jobSize = unpacker.unpackDouble();
                    netJobSize += jobSize;
                    jobCount++;
                } else {
                    unpacker.unpackString();
                    unpacker.unpackDouble();
                }
                
            }
            unpacker.close();

            System.out.println("Final time: " + newT);
            System.out.println("Number of jobs: " + jobCount);
            System.out.println("Mean interarrival time: " + newT.divide(new BigDecimal(batchCount), MathContext.DECIMAL64)); //can choose precision
            System.out.println("Mean batch size: " + jobCount/(double)batchCount);
            System.out.println("Mean job size " + netJobSize/jobCount);
            return 0;
        }
    }

    @Command(name = "merge", description = "Merge multiple workloads.")
    static class Merge implements Callable<Integer> {
        @ParentCommand private Workload parentWorkload;

        @Parameters(index = "0") private String newFileName;
        @Parameters(index = "1..*") private ArrayList<String> fileNames;
        
        @Override public Integer call() throws Exception {
            System.out.println("Merging multiple workloads");

            class Unpacker implements Comparable {
                private MessageUnpacker unpacker;
                private String streamLabel;
                private BigDecimal time;
                private double jobSize;

                public Unpacker (MessageUnpacker unpacker) throws IOException {
                    this.unpacker = unpacker;
                    streamLabel = unpacker.unpackString();
                    time = new BigDecimal(unpacker.unpackString());
                    jobSize = unpacker.unpackDouble();
                }

                public int compareTo(Object o) {
                    return time.compareTo(((Unpacker)o).time);
                }

                public void update() throws IOException {
                    streamLabel = unpacker.unpackString();
                    time = new BigDecimal(unpacker.unpackString());
                    jobSize = unpacker.unpackDouble();
                }

                public void write(MessagePacker packer) throws IOException {
                    packer.packString(streamLabel);
                    packer.packString(time.toString());
                    packer.packDouble(jobSize);
                    
                }
                public boolean hasNext() throws IOException {
                    return unpacker.hasNext();
                }

                public void close () throws IOException {
                    unpacker.close();
                }
            }
            MessagePacker packer = MessagePack.newDefaultPacker(new FileOutputStream(newFileName));

            PriorityQueue<Unpacker> unpackerPQ = new PriorityQueue<Unpacker>();
            HashMap<String, Double> streamToMJS = new HashMap<String, Double>();
            HashMap<String, Double> streamToQoS = new HashMap<String, Double>();
            for (String fileName : fileNames) {
                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(fileName));
                int n = unpacker.unpackInt();
                for (int i = 0; i < n; i++) {
                    String streamLabel = unpacker.unpackString();
                    streamToMJS.put(streamLabel, unpacker.unpackDouble());
                    streamToQoS.put(streamLabel, unpacker.unpackDouble());
                }
                unpackerPQ.add(new Unpacker(unpacker));
            }
            if(streamToMJS.size() != streamToQoS.size()) {
                System.out.println("Error: size of streamToQoS != size of streamToMJS");
            }
            packer.packInt(streamToMJS.size());
            for (String streamLabel : streamToMJS.keySet()) {
                packer.packString(streamLabel);
                packer.packDouble(streamToMJS.get(streamLabel));
                packer.packDouble(streamToQoS.get(streamLabel));
            }

            while(!unpackerPQ.isEmpty()) {
                Unpacker unp = unpackerPQ.poll();
                unp.write(packer);
                if(unp.hasNext()) {
                    unp.update();
                    unpackerPQ.add(unp);
                } else {
                    unp.close();
                }
            }
            packer.close();
            System.out.println("Merged workload generated");
            return 0;
        }
    }

    @Command(name = "simulate", description = "Simulate a federation.")
    //modeled after https://users.cs.northwestern.edu/~agupta/_projects/networking/QueueSimulation/mm1.html
    static class Simulate implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Parameters (index = "0") private String configYAML;
        @Parameters (index = "1") private String outputFileName;

        @Override public Integer call() throws Exception {
            System.out.println("Simulating an execution");
            YamlReader reader = new YamlReader(new FileReader(configYAML));
            String fileName = (String) ((Map)reader.read()).get("fileName");
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(fileName));

            HashMap<String, Double> streamToMJS = new HashMap<String, Double>();
            HashMap<String, Double> streamToQoS = new HashMap<String, Double>();
            int n = unpacker.unpackInt();
            for (int i = 0; i < n; i++) {
                String streamLabel = unpacker.unpackString();
                streamToMJS.put(streamLabel, unpacker.unpackDouble());
                streamToQoS.put(streamLabel, unpacker.unpackDouble());
            }

            ArrayList<Map> cloudMaps = new ArrayList<Map>();
            while (true) {
                Map cloud = (Map) reader.read();
                if (cloud == null) {
                    break;
                }
                cloudMaps.add(cloud);
            }
            ArrayList<Server> federationPool = new ArrayList<Server>();
            HashMap<String, Cloud> streamToCloud = new HashMap<String, Cloud>();
            ArrayList<Cloud> clouds = new ArrayList<Cloud>(); //seens unnecessary but will keep for now
            int j = 0;
            for (Map cloudMap: cloudMaps) {
                ArrayList<Server> serverPool = new ArrayList<Server>();
                for (Map serverSet : (ArrayList<Map>) cloudMap.get("serversets")) {
                    int count = Integer.parseInt((String)serverSet.get("count"));
                    double rate = Double.parseDouble((String)serverSet.get("rate"));
                    int shared = Integer.parseInt((String)serverSet.get("shared"));
                    int local = count - shared;
                    for (int i = 0 ; i < local; i++) {
                        serverPool.add(new Server(rate, j));
                        j++;
                    }
                    for (int i = 0; i < shared; i++) {
                        federationPool.add(new Server(rate, j));
                        j++;
                    }
                }
                Cloud cloud = new Cloud(serverPool, clouds.size());
                for (String streamLabel : (ArrayList<String>) cloudMap.get("streams")) {
                    streamToCloud.put(streamLabel, cloud);
                }
                clouds.add(cloud);
            }
            Federation federation = new Federation(federationPool);
            MessagePacker packer = MessagePack.newDefaultPacker(new FileOutputStream(outputFileName));
            CloudSimulator cloudSim = new CloudSimulator(unpacker, packer, federation, clouds, streamToCloud, streamToQoS, streamToMJS);
            cloudSim.doAllEvents();
            unpacker.close();
            packer.close();

            //basics
            basics = new ArrayList<Integer[]>();
            for (Cloud cloud : clouds) {
                Integer[] arr = new Integer[4];
                if (cloud.overflow + cloud.completed != cloud.arrivals){ 
                    throw new Exception ("cloud.overflow + cloud.completed != cloud.arrivals for cloud "+ basics.size());
                }
                arr[0] = cloud.arrivals;
                arr[1] = cloud.overflow;
                arr[2] = cloud.rejected;
                arr[3] = cloud.completed; //locally
                basics.add(arr);
            }

            //listeners
            listeners = new ArrayList<Integer[]>();
            for (Cloud cloud : clouds) {
                Listener l = cloud.listener;
                Integer[] arr = new Integer[4];
                arr[0] = l.arrivals;
                arr[1] = l.overflow;
                arr[2] = l.rejections;
                arr[3] = l.departures; //locally
                listeners.add(arr);
                //System.out.println(Arrays.asList(arr));
            }

            for (int i = 0; i < basics.size(); i++) {
                Integer[] arrB = basics.get(i);
                Integer[] arrL = listeners.get(i);
                for (int k = 0; k < 3; k++) {
                    if (!arrB[k].equals(arrL[k])) {
                        System.out.println(arrB[k] + " (basics) not equals " + arrL[k] + " (listeners)");
                        throw new Exception("Basics not equal to listener for cloud " + i + " at position " + k); 
                        //0 arrival. 1 overflow. 2 rejected. 3 departure (locally)
                    }
                }
            }
            System.out.println("Finished simulating");
            return 0;
        }
    }

    @Command(name = "metrics", description = "Compute metrics from simulation output.")
    static class Metrics implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Parameters (index = "0") private String inputFileName;
        @Parameters (index = "1") private int steadyState;
        @Option(names = {"-U", "--unitTest"}) private boolean unitTest;
        @Option(names = {"-E", "--endToEnd"}) private boolean endToEnd;
        @Option(names = {"-C", "--chart"}) private String chartSettings;
        @Option(names = {"-B", "--batching"}) private int batching;


        @Override public Integer call() throws Exception {
            System.out.println("Computing metrics");
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(inputFileName));
            int c = unpacker.unpackInt(); //amount of clouds
            for (int i = 0; i < c; i++) {
                int cloudID = unpacker.unpackInt();
                //System.out.println("cloud"+cloudID);
                int n = unpacker.unpackInt(); //cloud server count
                for (int j = 0; j < n; j++) {
                    int serverID = unpacker.unpackInt();
                    double workRate = unpacker.unpackDouble();
                    //System.out.println("\tserver" + serverID + " - work rate: " + workRate);
                }
            }
            int fedID = unpacker.unpackInt();
            if (fedID != -1) {
                throw new Exception("something went big wrong bc fed ID aint -1");
            }
            //System.out.println("fed");
            int m = unpacker.unpackInt(); //fed server count
            for (int i = 0; i < m; i++) {
                int serverID = unpacker.unpackInt();
                double workRate = unpacker.unpackDouble();
                //System.out.println("\tserver" + serverID + " - work rate: " + workRate);
            }
            int[] departures = new int [c];
            int[] overflow = new int [c];
            int[] rejections = new int[c]; 
            int[] arrivals = new int [c];
            int[] localDepartures = new int [c];
            int[] fedDeparturesCloud = new int [c];
            int fedArrivals = 0;
            int fedDepartures = 0;
            int fedRejections = 0;
            HashMap<Integer, Integer> requestToCloud = new HashMap<Integer, Integer>();
            int xzoom = 1;
            TimeSeriesCollection dataset = new TimeSeriesCollection();  
            TimeSeries netRej = new TimeSeries("Net Rejection Rate");  
            TimeSeries netFor = new TimeSeries("Net Forwarding Rate");  
            int totalDepartures = 0;

            int lastX = 20000; // last X for cumulative
            int chartInterval = 5000; //interval for adding data to chart
            if (chartSettings != null) {
                int colon = chartSettings.indexOf(":");
                lastX = Integer.parseInt(chartSettings.substring(0, colon));
                chartInterval = Integer.parseInt(chartSettings.substring(colon+1));
            }

            TimeSeries lastRej = new TimeSeries("Last " + lastX + " Rejection Rate");
            TimeSeries lastFor = new TimeSeries("Last " + lastX + " Forwarding Rate");
            int currSum = 0;
            Queue<Integer> toRemove = new LinkedList<Integer>();

            ArrayList<Double> batchListRej = new ArrayList<Double>();
            ArrayList<Double> batchListFor = new ArrayList<Double>();
            int batchRej = 0;
            int batchFor = 0;
            int batchArr = 0;
            int[] batchRejCloud = new int [c];
            int[] batchForCloud = new int [c];
            int[] batchArrCloud = new int [c];
            ArrayList<Double>[] batchListRejCloud = (ArrayList<Double>[]) new ArrayList<?>[c];
            ArrayList<Double>[] batchListForCloud = (ArrayList<Double>[]) new ArrayList<?>[c];
            for (int i = 0; i < c; i++) {
                batchListRejCloud[i] = new ArrayList<Double>();
                batchListForCloud[i] = new ArrayList<Double>();
            }
            while (unpacker.hasNext()) {
                int requestID = unpacker.unpackInt();
                int poolID = unpacker.unpackInt();
                String time = unpacker.unpackString();
                Noise.Type type = Noise.Type.valueOf(unpacker.unpackString());
                if (requestID >= steadyState) {
                    switch (type) {
                        case ARR: 
                            //double jobSize = unpacker.unpackDouble();
                            //String streamLabel = unpacker.unpackString();
                            if (poolID != -1) {
                                requestToCloud.put(requestID, poolID);
                            } else {
                                throw new Exception ("Arrival is not thrown by arrival to the federation");
                            }
                            arrivals[poolID]++;
                            if (batching != 0) {
                                batchArr += 1;
                                batchArrCloud[poolID] += 1;
                                if (batchArr >= batching) {
                                    batchListRej.add(batchRej/(double)batchArr);
                                    batchListFor.add(batchFor/(double)batchArr);
                                    batchArr = 0;
                                    batchRej = 0;
                                    batchFor = 0;
                                }
                                if (batchArrCloud[poolID] >= batching) {
                                    batchListRejCloud[poolID].add(batchRejCloud[poolID]/(double)batchArrCloud[poolID]);
                                    batchListForCloud[poolID].add(batchForCloud[poolID]/(double)batchArrCloud[poolID]);
                                    batchArrCloud[poolID] = 0;
                                    batchRejCloud[poolID] = 0;
                                    batchForCloud[poolID] = 0;
                                }
                            }
                            break;
                        case DEP:
                            //int serverID = unpacker.unpackInt();
                            if (poolID == -1) {
                                fedDepartures++;
                                fedDeparturesCloud[requestToCloud.get(requestID)]++;
                                departures[requestToCloud.get(requestID)]++;
                            } else {
                                localDepartures[poolID]+=1;
                                departures[poolID]+=1;
                            }
                            totalDepartures += 1;
                            if (chartSettings != null) {
                                currSum += 0;
                                toRemove.add(0);
                                if (toRemove.size() > lastX) {
                                    currSum -= toRemove.poll();
                                }
                                if ((totalDepartures+fedRejections)%chartInterval == 0) {
                                    //System.out.println(totalDepartures + fedRejections);
                                    netRej.add(new FixedMillisecond(xzoom*(totalDepartures + fedRejections)), fedRejections/(double)(totalDepartures+fedRejections));
                                    lastRej.add(new FixedMillisecond(xzoom*(totalDepartures + fedRejections)), currSum/(double)toRemove.size());
                                }
                            }
                            requestToCloud.remove(requestID);
                            break;
                        case REJ:
                            fedRejections++;
                            rejections[requestToCloud.get(requestID)]++;
                            if (chartSettings != null) {
                                currSum += 1;
                                toRemove.add(1);
                                if (toRemove.size() > lastX) {
                                    currSum -= toRemove.poll();
                                }
                                if ((fedRejections+totalDepartures)%chartInterval == 0) {
                                    //System.out.println(totalDepartures + fedRejections);
                                    netRej.add(new FixedMillisecond(xzoom*(totalDepartures + fedRejections)), fedRejections/(double)(totalDepartures+fedRejections));
                                    lastRej.add(new FixedMillisecond(xzoom*(totalDepartures + fedRejections)), currSum/(double)toRemove.size());
                                }
                            }
                            if (batching != 0) {
                                batchRej += 1;
                                batchRejCloud[poolID] += 1;
                            }
                            requestToCloud.remove(requestID);
                            break;
                        case ENQ:
                            break;
                        case SER:
                            //int serverID = unpacker.unpackInt();
                            break;
                        case OVR: 
                            overflow[poolID]++;
                            fedArrivals++;
                            if (batching != 0) {
                                batchFor += 1;
                                batchForCloud[poolID] += 1;
                            }
                            break;
                    }
                }
            }
            dataset.addSeries(netRej);
            dataset.addSeries(lastRej);
            //dataset.addSeries(netFor);
            //dataset.addSeries(netRej);
            
            int completed = 0;
            metrics = new ArrayList<Integer[]>();
            for (int i = 0; i < c; i++) {
                if (arrivals[i] != departures[i] + rejections[i]) {
                    throw new Exception ("Arrivals != departures + rejections for cloud " + i);
                }
                if (arrivals[i] != localDepartures[i] + overflow[i]) {
                    throw new Exception ("Arrivals != local departures + overflow for cloud " + i);
                }
                if (departures[i] != localDepartures[i] + fedDeparturesCloud[i]) {
                    throw new Exception ("Departures != local departures + fed departures for cloud " + i);
                }
                System.out.println("cloud"+i);
                System.out.println("\tArrivals:" + arrivals[i]);
                System.out.println("\tOverflow:" + overflow[i] + " - Rate: " + overflow[i]/(double)arrivals[i]);
                System.out.println("\tRejected:" + (rejections[i]) + " - Rate: " + (rejections[i])/(double)arrivals[i]);
                System.out.println("\tCompleted locally:" + (localDepartures[i])  + " - Rate: " + (localDepartures[i])/(double)arrivals[i]);
                completed += localDepartures[i];
                Integer[] arr = new Integer[3];
                arr[0] = overflow[i];
                arr[1] = rejections[i];
                arr[2] = localDepartures[i];
                metrics.add(arr);
            }
            completed += fedDepartures;
            if (fedRejections + fedDepartures != fedArrivals) {
                throw new Exception ("Fed rejections + fed departures != fed arrivals");
            }
            System.out.println("Completed by federation:" + fedDepartures);
            System.out.println("Rejected by federation:" + fedRejections);
            System.out.println("Net rejection rate:" + (fedRejections/(double)(completed+fedRejections)));

            if (unitTest) {
                if (!unitTest(metrics.get(0), metrics.get(1))) {
                    throw new Exception ("Failed Unit Test");
                } else {
                    System.out.println("Passed unit test");
                }
            } 

            if (endToEnd) { 
                System.out.println("E2E");
            }

            if (chartSettings != null) {
                chart = new TimeSeriesChart("Rejection Rate over Time", dataset);  
                /*
                chart.setSize(800, 400);  
                chart.setLocationRelativeTo(null);
                chart.setVisible(true);  
                chart.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                */
                ChartUtils.saveChartAsPNG(new File("soft3d.png"), chart.chart, 1200, 900);

            }

            if (batching != 0) {
                System.out.println("Federation rejection rate");
                printerval(batchListRej, 1.96);
                for (int i = 0; i < c; i++) {
                    System.out.println("Cloud " + i + " rejection rate");
                    printerval(batchListRejCloud[i], 1.96);
                }
                System.out.println("Federation forwarding rate");
                printerval(batchListFor, 1.96);
                for (int i = 0; i < c; i++) {
                    System.out.println("Cloud " + i + " forwarding rate");
                    printerval(batchListForCloud[i], 1.96);
                }
            }
            return 0;
        }
    }

    public static boolean unitTest (Integer[] metrics1, Integer [] metrics2) {
        //System.out.println(Arrays.asList(metrics1));
        //System.out.println(Arrays.asList(metrics2));
        return metrics1[0].equals(1056) && metrics1[1].equals(28) && metrics1[2].equals(3212) && metrics2[0].equals(2014) && metrics2[1].equals(196) && metrics2[2].equals(0);
    }

    //see https://rossetti.github.io/RossettiArenaBook/ch5-BatchMeansMethod.html#ref-kelton2004simulation
    //lag 1 correlation
    public static double correlation (int k, double yBar, ArrayList<Double> yList) {
        double t1 = Math.sqrt((k*k-1)/(double)(k-2)); //term 1
        double pNum = 0;//numerator
        for (int j = 0; j < k-1; j++) {
            pNum += (yList.get(j)-yBar) * (yList.get(j+1)-yBar);
        }
        double pDen = 0;//denominator
        for (int j = 0; j < k; j++) {
            pDen += Math.pow(yList.get(j)-yBar, 2);
        }
        double p1 = pNum/pDen;
        double fracNum = Math.pow(yList.get(0)-yBar, 2) + Math.pow(yList.get(k-1)-yBar, 2);
        double fracDen = 2 * pDen;
        double frac = fracNum/fracDen;
        double t2 = p1 + frac; //term 2
        return t1 * t2;
    } 

    public static void printerval (ArrayList<Double> batchList, double tStat) {
        int k = batchList.size();
        double yBar = 0;
        for (int i = 0; i < k; i++) {
            yBar += batchList.get(i);
        }
        yBar /= k;
        double corr = correlation(k, yBar, batchList);
        double var = 0;
        for (int i = 0; i < k; i++){
            var += Math.pow(yBar-batchList.get(i), 2);
        }
        var /= (k-1);
        double stdev = Math.sqrt(var);
        double halfWidth = tStat * stdev/Math.sqrt((double)k);
        System.out.println("---------------");
        System.out.println(k + " batches");
        System.out.println("Correlation: " + corr);
        System.out.println("Mean: " + yBar);
        System.out.println("Halfwidth: " + halfWidth);
        System.out.println("Lower bound: " + (yBar - halfWidth));
        System.out.println("Upper bound: " + (yBar + halfWidth));
        System.out.println("---------------");
    }

    
    //------------------------------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------------------------------
    
    @Command(name = "experiment", description = "Perform an experiment in the simulation.")
    static class Experiment implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Parameters (index = "0") private String seedString; //Long format
        @Parameters (index = "1") private int runID;
        @Parameters (index = "2") private String workloadString;
        @Parameters (index = "3") private String simulateString;
        @Parameters (index = "4") private int cutoff;
        @Parameters (index = "5") private int checkingInterval;
        @Parameters (index = "6") private String metricType;
        @Parameters (index = "7") private int metricCloud;
        @Parameters (index = "8") private double halfWidthReq;
        @Parameters (index = "9") private double alpha; //for tstat and confidence interval
        @Parameters (index = "10") private int batchSize;

        //------------------------------------------------------------------------------------------------------------------------------------------

        @Override public Integer call() throws Exception {
            boolean exit = false;
            seed = Long.parseLong(seedString);
            rng = RGF.create(seed);
            int cutoffPrime = cutoff;
            //int batching = checkingInterval/20;
            int batching = batchSize;
            double tStat = 1.96; //DO THIS DO THIS
            ArrayList<Double> metricArray = null;
            ArrayList<Double> batchListRej = null;
            ArrayList<Double> batchListFor = null;
            ArrayList<Double>[] batchListRejCloud = null;
            ArrayList<Double>[] batchListForCloud = null;
            String workloadFileName = "target/workload" + runID + ".txt";
            String outputFileName = "target/output" + runID + ".txt";

            //------------------------------------------------------------------------------------------------------------------------------------------
            //------------------------------------------------------------------------------------------------------------------------------------------

            ArrayList<WorkloadStream> streams = new ArrayList<WorkloadStream>();
            HashMap<String, Double> streamToMJS = new HashMap<String, Double>();
            HashMap<String, Double> streamToQoS = new HashMap<String, Double>();

            ArrayList<String> lamb = new ArrayList<String>();
            ArrayList<String> Q = new ArrayList<String>();
            String[] streamStrings = workloadString.split("\\|");
            for (String s : streamStrings) {
                //streamLabel
                int colon0 = s.indexOf(":");
                String streamLabel = s.substring(0, colon0);
                s = s.substring(colon0 + 1);

                //------------------------------------------------------------------------------------------------------------------------------------------

                //QoS
                int colon00 = s.indexOf(":");
                double QoS = Double.parseDouble(s.substring(0, colon00));
                Q.add(s.substring(0, colon00));
                s = s.substring(colon00 + 1);

                //------------------------------------------------------------------------------------------------------------------------------------------

                //ArrivalProcess
                ArrivalProcess arrivalProcess = null;
                int colon1 = s.indexOf("]") + 1;
                String APString = s.substring(0, colon1);
                if(Pattern.matches("Exp\\[\\d+(\\.\\d+)?\\]", APString)) { //Poisson
                    arrivalProcess = new PoissonArrivalProcess(rng, Double.parseDouble(APString.substring(4, colon1 - 1)));
                } else if(Pattern.matches("Reg\\[\\d+(\\.\\d+)?\\]", APString)) { //Periodic
                    arrivalProcess = new PeriodicArrivalProcess(Double.parseDouble(APString.substring(4, colon1 - 1)));
                } else {
                    System.out.println("Invalid arrival process distribution");
                }
                lamb.add(APString.substring(4, colon1 - 1));
                
                //------------------------------------------------------------------------------------------------------------------------------------------

                //BatchSizer
                BatchSizer batchSizer = null;
                String s2 = s.substring(colon1 + 1);
                int colon2 = s2.indexOf("]") + 1;
                String BSString = s2.substring(0, colon2);
                if (Pattern.matches("Det\\[\\d+\\]", BSString)) { //Deterministic
                    batchSizer = new DetBatchSizer(Integer.parseInt(BSString.substring(4, colon2 - 1)));
                } else if (Pattern.matches("Dist\\[0\\.\\d+:\\d+(\\.\\d+)?(,0\\.\\d+:\\d+(\\.\\d+)?)*\\]", BSString)) { //Distributed
                    int entries = 1 + (int) (BSString.chars().filter(ch -> ch == ',').count());
                    double[] probabilities = new double[entries];
                    int[] batchSizes = new int[entries];
                    BSString = BSString.substring(5);
                    int stop = 0;
                    for (int i = 0; i < entries; i++) {
                        stop = BSString.indexOf(":");
                        probabilities[i] = Double.parseDouble(BSString.substring(0, stop));
                        BSString = BSString.substring(stop + 1);
                        stop = BSString.indexOf(",") < 0 ? BSString.indexOf("]") : BSString.indexOf(",");
                        batchSizes[i] = Integer.parseInt(BSString.substring(0, stop));
                        BSString = BSString.substring(stop + 1);
                    }
                    batchSizer = new DistBatchSizer(rng, probabilities, batchSizes);
                } else {
                    System.out.println("Invalid batch sizer distribution");
                }

                //------------------------------------------------------------------------------------------------------------------------------------------

                //JobGenerator
                JobGenerator jobGenerator = null;
                String s3 = s2.substring(colon2 + 1);
                if (Pattern.matches("Unif\\[\\d+(\\.\\d+)?,\\d+(\\.\\d+)?\\]", s3)) { //Uniform
                    jobGenerator = new UniformJobGenerator(rng, Double.parseDouble(s3.substring(5, s3.indexOf(","))), 
                    Double.parseDouble(s3.substring(s3.indexOf(",") + 1, s3.length() - 1)));
                } else if (Pattern.matches("Par\\[\\d+(\\.\\d+)?,\\d+(\\.\\d+)?\\]", s3)) { //Pareto
                    jobGenerator = new ParetoJobGenerator(rng, Double.parseDouble(s3.substring(4, s3.indexOf(","))),
                    Double.parseDouble(s3.substring(s3.indexOf(",") + 1, s3.length() - 1)));
                } else if (Pattern.matches("Exp\\[\\d+(\\.\\d+)?\\]", s3)) { //Exponential
                    jobGenerator = new ExponentialJobGenerator(rng, Double.parseDouble(s3.substring(4, s3.length()-1)));
                }
                else {
                    System.out.println("Invalid job generator distribution");
                }
                
                streamToMJS.put(streamLabel, jobGenerator.meanJobSize());
                streamToQoS.put(streamLabel, QoS);
                streams.add(new WorkloadStream(arrivalProcess, batchSizer, jobGenerator, streamLabel));
            }
            WorkloadGenerator generator = new WorkloadGenerator(streams, new BigDecimal(0));

            //------------------------------------------------------------------------------------------------------------------------------------------
            //------------------------------------------------------------------------------------------------------------------------------------------

            ArrayList<Server> federationPool = new ArrayList<Server>();
            HashMap<String, Cloud> streamToCloud = new HashMap<String, Cloud>();
            ArrayList<Cloud> clouds = new ArrayList<Cloud>();
            int j = 0;
            ArrayList<String> x = new ArrayList<String>();
            ArrayList<String> n = new ArrayList<String>();
            String[] cloudStrings = simulateString.split("\\|");
            for (String cloudString : cloudStrings) {
                String streamsString = cloudString.substring(0, cloudString.indexOf("!"));
                ArrayList<Server> serverPool = new ArrayList<Server>();
                String[] serverStrings = cloudString.substring(cloudString.indexOf("!")+1).split("/");
                for (String serverString : serverStrings) {
                    int colon0 = serverString.indexOf(":");
                    int count = Integer.parseInt(serverString.substring(0, colon0));
                    n.add(serverString.substring(0, colon0));
                    serverString = serverString.substring(colon0+1);
                    int colon1 = serverString.indexOf(":");
                    double rate = Double.parseDouble(serverString.substring(0, colon1));
                    serverString = serverString.substring(colon1+1);
                    int shared = Integer.parseInt(serverString);
                    x.add(serverString);
                    int local = count - shared;
                    for (int i = 0 ; i < local; i++) {
                        serverPool.add(new Server(rate, j));
                        j++;
                    }
                    for (int i = 0; i < shared; i++) {
                        federationPool.add(new Server(rate, j));
                        j++;
                    }
                }
                Cloud cloud = new Cloud(serverPool, clouds.size());
                for (String streamLabel : streamsString.split(":")) {
                    streamToCloud.put(streamLabel, cloud);
                }
                clouds.add(cloud);
            }

            Federation federation = new Federation(federationPool);
            CloudSimulator cloudSim = new CloudSimulator(federation, clouds, streamToCloud, streamToQoS, streamToMJS);
            MessagePacker workloadPacker;
            MessageUnpacker workloadUnpacker;
            MessagePacker outputPacker = MessagePack.newDefaultPacker(new FileOutputStream(outputFileName));
            MessageUnpacker outputUnpacker;

            while (!exit) {

                //System.out.println("Genenerating workload");
                workloadPacker = MessagePack.newDefaultPacker(new FileOutputStream(workloadFileName));
                generator.generateWorkload(workloadPacker, new JobStoppingCriterion(checkingInterval + cutoffPrime));
                workloadPacker.close();
                //System.out.println("Workload generated");

                //------------------------------------------------------------------------------------------------------------------------------------------
                //------------------------------------------------------------------------------------------------------------------------------------------

                //System.out.println("Simulating");
                workloadUnpacker = MessagePack.newDefaultUnpacker(new FileInputStream(workloadFileName));
                outputPacker = MessagePack.newDefaultPacker(new FileOutputStream(outputFileName, true));
                cloudSim.initiateSimulation(outputPacker, workloadUnpacker);
                cloudSim.doAllEvents2();
                workloadUnpacker.close();
                outputPacker.close();
                //System.out.println("Finished simulating");

                //------------------------------------------------------------------------------------------------------------------------------------------
                //------------------------------------------------------------------------------------------------------------------------------------------

                //System.out.println("Computing metrics");
                outputUnpacker = MessagePack.newDefaultUnpacker(new FileInputStream(outputFileName));
                int c = clouds.size();
                HashMap<Integer, Integer> requestToCloud = new HashMap<Integer, Integer>();
                int batchRej = 0;
                int batchFor = 0;
                int batchArr = 0;
                int[] batchRejCloud = new int [c];
                int[] batchForCloud = new int [c];
                int[] batchArrCloud = new int [c];
                batchListRej = new ArrayList<Double>();
                batchListFor = new ArrayList<Double>();
                batchListRejCloud = (ArrayList<Double>[]) new ArrayList<?>[c];
                batchListForCloud = (ArrayList<Double>[]) new ArrayList<?>[c];
                for (int i = 0; i < c; i++) {
                    batchListRejCloud[i] = new ArrayList<Double>();
                    batchListForCloud[i] = new ArrayList<Double>();
                }

                //------------------------------------------------------------------------------------------------------------------------------------------

                int zeroes = 0;
                while (outputUnpacker.hasNext()) {
                    int requestID = outputUnpacker.unpackInt();
                    int poolID = outputUnpacker.unpackInt();
                    String time = outputUnpacker.unpackString();
                    Noise.Type type = Noise.Type.valueOf(outputUnpacker.unpackString());
                    if (requestID == 0 && type.equals(Noise.Type.ARR)) {
                        zeroes++;
                    }
                    if (requestID >= cutoff || zeroes > 1) {
                        switch (type) {
                            case ARR:
                                requestToCloud.put(requestID, poolID);
                                batchArr += 1;
                                batchArrCloud[poolID] += 1;
                                if (batchArr >= batching) {
                                    batchListRej.add(batchRej/(double)batchArr);
                                    batchListFor.add(batchFor/(double)batchArr);
                                    batchArr = 0;
                                    batchRej = 0;
                                    batchFor = 0;
                                }
                                if (batchArrCloud[poolID] >= batching) {
                                    batchListRejCloud[poolID].add(batchRejCloud[poolID]/(double)batchArrCloud[poolID]);
                                    batchListForCloud[poolID].add(batchForCloud[poolID]/(double)batchArrCloud[poolID]);
                                    batchArrCloud[poolID] = 0;
                                    batchRejCloud[poolID] = 0;
                                    batchForCloud[poolID] = 0;
                                }
                                break;
                            case DEP:
                                requestToCloud.remove(requestID);
                                break;
                            case REJ:
                                batchRej += 1;
                                batchRejCloud[poolID] += 1;
                                requestToCloud.remove(requestID);
                                break;
                            case ENQ:
                                break;
                            case SER:
                                break;
                            case OVR: 
                                batchFor += 1;
                                batchForCloud[poolID] += 1;
                                break;
                        }
                    }
                }

                //------------------------------------------------------------------------------------------------------------------------------------------

                if (metricType.equals("csv")) {
                    exit = exitCondition(batchListRej, tStat, halfWidthReq) && exitCondition(batchListFor, tStat, halfWidthReq);
                } else {
                    //java -jar target/cloudfed.jar experiment "0" 0 "type1:0:Exp[50]:Det[1]:Unif[1,1]|type2:0:Exp[50]:Det[1]:Unif[1,1]" "type1!50:1:0|type2!50:1:0" 1000000 1000000 "rej" -1 0.0004 0.05 100000
                    if (metricType.equals("rej")) {
                        if (metricCloud == -1) {
                            metricArray = batchListRej;
                        } else {
                            metricArray = batchListRejCloud[metricCloud];
                        }
                    } else if (metricType.equals("for")) {
                        if (metricCloud == -1) {
                            metricArray = batchListFor;
                        } else {
                            metricArray = batchListForCloud[metricCloud];
                        }
                    } else {
                        throw new Exception("invalid metric type");
                    }
                    exit = exitCondition(metricArray, tStat, halfWidthReq);
                    printerval(metricArray, tStat);
                }
                cutoffPrime = 0;
                //System.out.println("Finished metrics\n");
            }
            //System.out.println("Experiment complete");
            if (metricType.equals("csv")) {
                String sc_overflow1 = "" + (50*mean(batchListForCloud[0]));
                String sc_overflow2 = "" + (50*mean(batchListForCloud[1]));
                String fed_overflow1 = "" + (50*mean(batchListRejCloud[0]));
                String fed_overflow2 = "" + (50*mean(batchListRejCloud[1]));
                String overflowReduction = ""+((mean(batchListFor) - mean(batchListRej))/mean(batchListFor));
                CSVWriter writer = new CSVWriter(new FileWriter(new File("results.csv"), true));
                String[] data = {lamb.get(0), x.get(0), n.get(0), Q.get(0), /*mst.get(0)*/"1", lamb.get(1), x.get(1), n.get(1), Q.get(1), /*mst.get(1)*/"1", overflowReduction, sc_overflow1, sc_overflow2, fed_overflow1, fed_overflow2};
                writer.writeNext(data);
                writer.close();
                System.out.println(x.get(0)+":"+x.get(1));
            }
            return 0;
        }
    }

    public static double mean (ArrayList<Double> batchList) {
        int k = batchList.size();
        double yBar = 0;
        for (int i = 0; i < k; i++) {
            yBar += batchList.get(i);
        }
        yBar /= k;
        return yBar;
    }

    public static boolean exitCondition(ArrayList<Double> batchList, double tStat, double halfWidthReq) {
        return correlationCheck(batchList, tStat) && halfWidthCheck(batchList, tStat, halfWidthReq);
    }

    public static boolean correlationCheck (ArrayList<Double> batchList, double tStat) {
        int k = batchList.size();
        return Math.abs(correlation(k, mean(batchList), batchList)) < tStat;
    }

    public static boolean halfWidthCheck (ArrayList<Double> batchList, double tStat, double halfWidthReq) {
        int k = batchList.size();
        double yBar = mean(batchList);
        double var = 0;
        for (int i = 0; i < k; i++){
            var += Math.pow(yBar-batchList.get(i), 2);
        }
        var /= (k-1);
        double stdev = Math.sqrt(var);
        double halfWidth = tStat * stdev/Math.sqrt((double)k);
        return halfWidth < halfWidthReq;
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}

