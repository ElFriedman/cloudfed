package edu.usc.qed.cloudfed.Simulate;

import java.util.HashMap;
import java.io.IOException;
import java.math.BigDecimal;

import org.msgpack.core.MessageUnpacker;

public class Generator extends Event {
    public MessageUnpacker unpacker;
    public HashMap<String, Cloud> streamToCloud;
    public Request nextRequest;

    public Generator (MessageUnpacker unpacker, HashMap<String, Cloud> streamToCloud) throws IOException {
        this.unpacker = unpacker;
        this.streamToCloud = streamToCloud;
        nextRequest = getRequest();
    }

    public void execute (AbstractSimulator simulator) {
        nextRequest.serverPool.insert(simulator, nextRequest);
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
        Cloud cloud = streamToCloud.get(streamLabel);
        this.time = time;
        return new Request (streamLabel, time, jobSize, cloud);
    }
}
