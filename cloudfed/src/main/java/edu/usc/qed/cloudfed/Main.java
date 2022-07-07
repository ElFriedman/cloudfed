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

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.esotericsoftware.yamlbeans.YamlReader;


@Command(name = "cloudfed", mixinStandardHelpOptions = true, version = "cloudfed 1.0",
         description = "Cloud Federation Simulator",
         scope = ScopeType.INHERIT,
         subcommands = { /*Main.Seed.class,*/ Main.Workload.class, Main.Simulate.class, Main.Metrics.class })
public class Main {
    private final static Logger logger =  LoggerFactory.getLogger(Main.class); //what is this for
    private static long seed = 0;
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

            //setting seed
            if (seedString != null) {
                seed = Long.parseLong(seedString);
                rng = RGF.create(seed);
            } else {
                seed = Long.parseLong("" + ((int)(1000000*Math.random())));
                rng = RGF.create(seed);
            }

            //stopping criteria
            StoppingCriterion stoppingCriterion = null;
            if (Pattern.matches("Job\\[\\d+\\]", stopCritString)) { //job criterion
                stoppingCriterion = new JobStoppingCriterion(Integer.parseInt(stopCritString.substring(4, stopCritString.length()-1)));
            } else if (Pattern.matches("Time\\[\\d+(\\.\\d+)?\\]", stopCritString)) { //time criterion
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
                } else {
                    System.out.println("Invalid arrival process distribution");
                }
                
                //BatchSizer
                BatchSizer batchSizer = null;
                String s2 = s.substring(colon1 + 1);
                int colon2 = s2.indexOf("]") + 1;
                String BSString = s2.substring(0, colon2);
                if (Pattern.matches("Det\\[\\d+\\]", BSString)) { //Determined
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
                if (Pattern.matches("Unif\\[\\d+,\\d+\\]", s3)) { //Uniform
                    jobGenerator = new UniformJobGenerator(rng, Double.parseDouble(s3.substring(5, s3.indexOf(","))), 
                    Double.parseDouble(s3.substring(s3.indexOf(",") + 1, s3.length() - 1)));
                } else {
                    System.out.println("Invalid job generator distribution");
                }

                streamToMJS.put(streamLabel, jobGenerator.meanJobSize());
                streamToQoS.put(streamLabel, QoS);

                streams.add(new WorkloadStream(arrivalProcess, batchSizer, jobGenerator, streamLabel));
            }
            WorkloadGenerator generator = new WorkloadGenerator(streams);

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
                Integer[] arr = new Integer[3];
                arr[0] = cloud.overflow;
                arr[1] = cloud.rejected;
                arr[2] = cloud.completed;
                basics.add(arr);
                //System.out.println(Arrays.toString(arr));
            }

            //listeners
            listeners = new ArrayList<Integer[]>();
            for (Cloud cloud : clouds) {
                Listener l = cloud.listener;
                Integer[] arr = new Integer[3];
                arr[0] = l.overflow;
                arr[1] = l.arrivals - l.departures;
                arr[2] = l.arrivals - l.overflow;
                listeners.add(arr);
                //System.out.println(Arrays.toString(arr));
            }

            for (int i = 0; i < basics.size(); i++) {
                Integer[] arrB = basics.get(i);
                Integer[] arrL = listeners.get(i);
                for (int k = 0; k < 3; k++) {
                    if (!arrB[k].equals(arrL[k])) {
                        System.out.println(arrB[k] + " (basics) not equals " + arrL[k] + " (listeners)");
                        throw new Exception("Basics not equal to listener for cloud " + i + " at position " + k); 
                    }
                }
            }

            return 0;
        }
    }

    @Command(name = "metrics", description = "Compute metrics from simulation output.")
    static class Metrics implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Parameters (index = "0") private String inputFileName;
        @Option(names = {"-U", "--unitTest"}) private boolean unitTest;
        @Option(names = {"-E", "--endToEnd"}) private boolean endToEnd;

        @Override public Integer call() throws Exception {
            System.out.println("Computing metrics");
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(inputFileName));
            int c = unpacker.unpackInt();
            for (int i = 0; i < c; i++) {
                int cloudID = unpacker.unpackInt();
                //System.out.println("cloud"+cloudID);
                int n = unpacker.unpackInt();
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
            int m = unpacker.unpackInt();
            for (int i = 0; i < m; i++) {
                int serverID = unpacker.unpackInt();
                double workRate = unpacker.unpackDouble();
                //System.out.println("\tserver" + serverID + " - work rate: " + workRate);
            }
            int[] departures = new int [c]; //total completed
            int[] overflow = new int [c]; //overflow
            int[] arrivals = new int [c];
            //hence rejected = A-D, completed locally = A-R, and fed completed = D-R
            int fedArrivals = 0;
            int fedDepartures = 0;
            int fedRejections = 0;
            HashMap<Integer, Integer> requestToCloud = new HashMap<Integer, Integer> ();

            TimeSeriesCollection dataset = new TimeSeriesCollection();  
            TimeSeries net = new TimeSeries("Series1");  
            int totalDepartures = 0;
            TimeSeries lastX = new TimeSeries("Series1");
            int x = 10000;
            int currSum = 0;
            Queue<Integer> toRemove = new LinkedList<Integer>();

            while (unpacker.hasNext()) {
                int requestID = unpacker.unpackInt();
                int poolID = unpacker.unpackInt();
                String time = unpacker.unpackString();
                Noise.Type type = Noise.Type.valueOf(unpacker.unpackString());
                int serverID;
                switch (type) {
                    case ARR:
                        if (poolID == -1) {
                            fedArrivals++;
                        } else {
                            arrivals[poolID]+=1;
                        }
                        requestToCloud.put(requestID, poolID);
                        double jobSize = unpacker.unpackDouble();
                        String streamLabel = unpacker.unpackString();
                        break;
                    case DEP:
                        serverID = unpacker.unpackInt();
                        if (poolID == -1) {
                            fedDepartures++;
                            departures[requestToCloud.get(requestID)]++;
                        } else {
                            departures[poolID]+=1;
                        }
                        totalDepartures += 1;
                        if (totalDepartures%100 == 0) {
                            net.add(new FixedMillisecond(totalDepartures + fedRejections), fedRejections/(double)totalDepartures);
                        }
                        currSum += 0;
                        toRemove.add(0);
                        if (toRemove.size() > x) {
                            currSum -= toRemove.poll();
                        }
                        if (totalDepartures%100 == 0) {
                            lastX.add(new FixedMillisecond(totalDepartures + fedRejections), currSum/(double)toRemove.size());
                        }
                        break;
                    case REJ:
                        if (poolID == -1) {
                            fedRejections++;

                            if (totalDepartures%100 == 0) {
                                net.add(new FixedMillisecond(totalDepartures + fedRejections), fedRejections/(double)totalDepartures);
                            }
                            currSum += 1;
                            toRemove.add(1);
                            if (toRemove.size() > x) {
                                currSum -= toRemove.poll();
                            }
                            if (totalDepartures%100 == 0) {
                                lastX.add(new FixedMillisecond(totalDepartures + fedRejections), currSum/(double)toRemove.size());
                            }
                        } else {
                            overflow[poolID]+=1;
                        }
                        break;
                    case ENQ:
                        break;
                    case SER:
                        serverID = unpacker.unpackInt();
                        break;
                }
            }

            dataset.addSeries(net);
            dataset.addSeries(lastX);

            int completed = 0;
            metrics = new ArrayList<Integer[]>();
            for (int i = 0; i < c; i++) {
                System.out.println("cloud"+i);
                System.out.println("\tOverflow:" + overflow[i]);
                System.out.println("\tRejected:" + (arrivals[i]-departures[i]));
                System.out.println("\tCompleted locally:" + (arrivals[i]-overflow[i]));
                completed += arrivals[i]-overflow[i];

                Integer[] arr = new Integer[3];
                arr[0] = overflow[i];
                arr[1] = arrivals[i] - departures[i];
                arr[2] = arrivals[i] - overflow[i];
                metrics.add(arr);
            }
            completed += fedDepartures;
            System.out.println("Completed by federation:" + fedDepartures);
            System.out.println("Rejected by federation:" + fedRejections);
            System.out.println("Net rejection rate:" + (fedRejections/(double)(completed+fedRejections)));

            /*for (int i = 0; i < basics.size(); i++) {
                Integer[] arrB = basics.get(i);
                Integer[] arrM = metrics.get(i);
                for (int k = 0; k < 3; k++) {
                    if (arrB[k] != arrM[k]) {
                        throw new Exception("Basics not equal to metrics for cloud " + i + " at position " + k); 
                    }
                }
            }*/


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

            
            chart = new TimeSeriesChart("Rejection Rate over Time", dataset);  
            chart.setSize(800, 400);  
            chart.setLocationRelativeTo(null);
            chart.setVisible(true);  
            chart.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            ChartUtils.saveChartAsPNG(new File("soft3d.png"), chart.chart, 400, 300);
            return 0;
        }
    }

    public static boolean unitTest (Integer[] metrics1, Integer metrics2[]) {
        return metrics1[0].equals(1054) && metrics1[1].equals(32) && metrics1[2].equals(3214) && metrics2[0].equals(2014) && metrics2[1].equals(189) && metrics2[2].equals(0);
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}

