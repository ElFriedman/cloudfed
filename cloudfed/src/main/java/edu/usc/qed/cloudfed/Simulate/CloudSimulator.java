package edu.usc.qed.cloudfed.Simulate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.msgpack.core.MessageUnpacker;

public class CloudSimulator extends Simulator {
    public MessageUnpacker unpacker;
    public HashMap<String, Cloud> streamToCloud;
    public Federation federation;
    public ArrayList<Cloud> clouds; //again, perhaps extraneous
    public HashMap<String, Double> streamToQoS;
    public HashMap<String, Double> streamToMJS;
    
    public CloudSimulator (MessageUnpacker unpacker, HashMap<String, Cloud> streamToCloud, Federation federation, 
    ArrayList<Cloud> clouds, HashMap<String, Double> streamToQoS, HashMap<String, Double> streamToMJS) throws IOException {
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
