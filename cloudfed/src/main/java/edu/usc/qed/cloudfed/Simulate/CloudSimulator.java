package edu.usc.qed.cloudfed.Simulate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

public class CloudSimulator extends Simulator {
    public MessageUnpacker unpacker;
    private MessagePacker packer;
    public Federation federation;
    public ArrayList<Cloud> clouds;
    public HashMap<String, Cloud> streamToCloud;
    public HashMap<String, Double> streamToQoS;
    public HashMap<String, Double> streamToMJS;
    
    public CloudSimulator (MessageUnpacker unpacker, MessagePacker packer, Federation federation, ArrayList<Cloud> clouds, 
    HashMap<String, Cloud> streamToCloud, HashMap<String, Double> streamToQoS, HashMap<String, Double> streamToMJS) throws IOException {
        events = new PriorityQueue<>();
        this.unpacker = unpacker;
        this.packer = packer;
        this.streamToCloud = streamToCloud;
        this.federation = federation;
        this.clouds = clouds;
        this.streamToQoS = streamToQoS;
        this.streamToMJS = streamToMJS;
        events.add(new Generator(unpacker, streamToCloud));
        logServers();
    }

    public void logServers () throws IOException {
        packer.packInt(clouds.size());
        for (Cloud cloud : clouds) {
            packer.packInt(cloud.ID);
            packer.packInt(cloud.freeServers.size());
            for (Server s : cloud.freeServers) {
                packer.packInt(s.ID);
                packer.packDouble(s.workRate);
            }
        }
        packer.packInt(federation.ID);
        packer.packInt(federation.freeServers.size());
        for (Server s : federation.freeServers) {
            packer.packInt(s.ID);
            packer.packDouble(s.workRate);
            //if possible maybe track which cloud server came from.
            //would make new Server constructor with extra int, referring to cloudID
        }
    }
    

    public void log (AbstractEvent e) throws IOException {
        Noise n = (Noise) e;
        packer.packInt(n.r.ID);
        packer.packInt(n.pool.ID);
        packer.packString(this.now().toString());
        Noise.Type t = n.getType();
        packer.packString(t.name());
        switch (t) {
            case ARR:
                packer.packDouble(n.r.jobSize);
                packer.packString(n.r.streamLabel);
                break;
            case DEP:
            packer.packInt(((Departure)n).server.ID);
                break;
            case REJ:
                break;
            case ENQ:
                break;
            case SER:
                packer.packInt(((Servicing)n).server.ID);
                break;
        }
    }
}
