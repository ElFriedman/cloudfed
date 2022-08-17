package edu.usc.qed.cloudfed.Simulate;

import java.io.IOException;
import java.util.HashMap;

import org.msgpack.core.MessageUnpacker;

public class Generator2 extends Generator {
    public Generator2 (MessageUnpacker unpacker, HashMap<String, Cloud> streamToCloud) throws IOException {
        super(unpacker, streamToCloud);
    }

    @Override //creates Ender to ensure clean joint
    public void execute (AbstractSimulator simulator) throws Exception {
        Arrival x = new Arrival(nextRequest, ((Simulator)simulator).now(), nextRequest.getCloud(simulator));
        x.execute(simulator);
        nextRequest.getCloud(simulator).insert(simulator, nextRequest);
        try {
            if (unpacker.hasNext()) {
                nextRequest = getRequest();
                simulator.insert(this);
            } else {
                simulator.insert(new Ender(time));
                ((CloudSimulator) simulator).generator = null;
            }
        } catch (IOException e) {
            System.out.println("Generator crashed trying to get new request");
            e.printStackTrace();
        }
    }
}
