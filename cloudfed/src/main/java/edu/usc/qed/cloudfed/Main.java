package edu.usc.qed.cloudfed;

import java.util.concurrent.Callable;
import java.util.random.RandomGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.qed.cloudfed.Workload.*;
import edu.usc.qed.cloudfed.Simulate.*;


import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.regex.Pattern;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ScopeType;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.esotericsoftware.yamlbeans.YamlReader;


@Command(name = "cloudfed", mixinStandardHelpOptions = true, version = "cloudfed 1.0",
         description = "Cloud Federation Simulator",
         scope = ScopeType.INHERIT,
         subcommands = { Main.Workload.class, Main.Simulate.class, Main.Metrics.class })
public class Main {
    private final static Logger logger =  LoggerFactory.getLogger(Main.class);

    @Option(names = "--verbose", defaultValue = "false", scope = ScopeType.INHERIT,
            description = "Log additional debugging information (default: ${DEFAULT-VALUE})")
    private boolean verbose;

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
        @Parameters(index = "1") private String stopCritString;
        @Parameters(index = "2..*") private ArrayList<String> streamStrings;
        
        @Override public Integer call() throws Exception {
            System.out.println("Genenerating a workload");
            
            RandomGenerator rng = RandomGenerator.getDefault();

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

            for (String s : streamStrings) {
                int colon0 = s.indexOf(":");
                String streamLabel = s.substring(0, colon0);
                s = s.substring(colon0 + 1);

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
                streams.add(new WorkloadStream(arrivalProcess, batchSizer, jobGenerator, streamLabel));
            }
            WorkloadGenerator generator = new WorkloadGenerator(streams);

            MessagePacker packer = MessagePack.newDefaultPacker(new FileOutputStream(fileName));
            packer.packInt(streamToMJS.size());
            for (String streamLabel : streamToMJS.keySet()) {
                packer.packString(streamLabel);
                packer.packDouble(streamToMJS.get(streamLabel));
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
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(fileName));
            int n = unpacker.unpackInt();
            HashMap<String, Boolean> streamBool = new HashMap<String, Boolean>();
            for (int i = 0; i < n; i++) {
                String streamLabel = unpacker.unpackString();
                streamBool.put(streamLabel, allStreams || streams.contains(streamLabel));
                unpacker.unpackDouble();
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
            for (String fileName : fileNames) {
                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(fileName));
                int n = unpacker.unpackInt();
                for (int i = 0; i < n; i++) {
                    streamToMJS.put(unpacker.unpackString(), unpacker.unpackDouble());
                }
                unpackerPQ.add(new Unpacker(unpacker));
            }

            packer.packInt(streamToMJS.size());
            for (String streamLabel : streamToMJS.keySet()) {
                packer.packString(streamLabel);
                packer.packDouble(streamToMJS.get(streamLabel));
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
            int n = unpacker.unpackInt();
            for (int i = 0; i < n; i++) {
                streamToMJS.put(unpacker.unpackString(), unpacker.unpackDouble());
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
            for (Map cloudMap: cloudMaps) {
                ArrayList<Server> serverPool = new ArrayList<Server>();
                for (Map serverSet : (Map[]) cloudMap.get("serversets")) {
                    int count = (int)serverSet.get("count");
                    double rate = (double)serverSet.get("rate");
                    int shared = (int)serverSet.get("shared");
                    for (int i = count ; i > 0; i--) {
                        if (i < shared) {
                            federationPool.add(new Server(rate));
                        } else {
                            serverPool.add(new Server(rate));
                        }
                    }
                }
                Cloud cloud = new Cloud(serverPool);
                for (String streamLabel : (String[]) cloudMap.get("streams")) {
                    streamToCloud.put(streamLabel, cloud);
                }
                clouds.add(cloud);
            }
            Federation federation = new Federation(federationPool);

            //DO THIS
            HashMap<String, Double> streamToQoS = null;
            //DO THIS

            CloudSimulator cloudSim = new CloudSimulator(unpacker, streamToCloud, federation, clouds, streamToQoS, streamToMJS);
            cloudSim.doAllEvents();
            unpacker.close();
            System.out.println("Finished simulation");
            return 0;
        }
    }

    @Command(name = "metrics", description = "Compute metrics from simulation output.")
    static class Metrics implements Callable<Integer> {
        @ParentCommand private Main parent;

        @Override public Integer call() throws Exception {
            System.out.println("Computing metrics");
            return 0;
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
