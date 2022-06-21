package edu.usc.qed.cloudfed.Simulate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.msgpack.core.MessageUnpacker;

public class CloudSimulator extends Simulator {
    public MessageUnpacker unpacker;
    public ArrayList<Cloud> clouds; //again, perhaps extraneous
    public Federation federation;
    public HashMap<String, Cloud> streamToCloud;
    public HashMap<String, Double> streamToQoS;
    public HashMap<String, Double> streamToMJS;
    
    public CloudSimulator (MessageUnpacker unpacker, Federation federation, 
    ArrayList<Cloud> clouds, HashMap<String, Cloud> streamToCloud, HashMap<String, Double> streamToQoS, HashMap<String, Double> streamToMJS) throws IOException {
        events = new PriorityQueue<>();
        this.unpacker = unpacker;
        this.streamToCloud = streamToCloud;
        this.federation = federation;
        this.clouds = clouds;
        this.streamToQoS = streamToQoS;
        this.streamToMJS = streamToMJS;
        events.add(new Generator(unpacker, streamToCloud));
    }
}
