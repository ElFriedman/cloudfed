package edu.usc.qed.cloudfed.Simulate;

import java.util.HashMap;
import java.io.IOException;
import java.math.BigDecimal;

import org.msgpack.core.MessageUnpacker;

public class Generator extends Event {
    public MessageUnpacker unpacker;
    public HashMap<String, Cloud> streamToCloud;
    public int lastID;
    public Request nextRequest;

    public Generator (MessageUnpacker unpacker, HashMap<String, Cloud> streamToCloud) throws IOException {
        this.unpacker = unpacker;
        this.streamToCloud = streamToCloud;
        lastID = -1;
        nextRequest = getRequest();
    }

    public void execute (AbstractSimulator simulator) throws Exception {
        nextRequest.getCloud(simulator).insert(simulator, nextRequest);
        simulator.insert(new Arrival(nextRequest, ((Simulator)simulator).now(), nextRequest.getCloud(simulator)));
        try {
            if (unpacker.hasNext()) {
                nextRequest = getRequest();
                simulator.insert(this);
            }
        } catch (IOException e) {
            System.out.println("Generator crashed trying to get new request");
            e.printStackTrace();
        }
    }

    public Request getRequest () throws IOException {
        String streamLabel = unpacker.unpackString();
        BigDecimal time = new BigDecimal(unpacker.unpackString());
        double jobSize = unpacker.unpackDouble();
        lastID++;
        this.time = time;
        return new Request (streamLabel, time, jobSize, lastID);
    }
}
