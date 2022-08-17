package edu.usc.qed.cloudfed;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import org.apache.commons.math3.stat.inference.TTest;

public class TestMessagePacker {

    public static void main (String [] args) throws IOException {
        String dooby = "dooby";
        String [] doo = dooby.split(":");
        for (String dooo : doo) {
            System.out.println(dooo);
        }
        String fileName = "target/x.txt";
        MessagePacker packer = MessagePack.newDefaultPacker(new FileOutputStream(fileName));
        packer.packString("beep");
        packer.close();
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(fileName));
        System.out.println(unpacker.unpackString());
        unpacker.close();
        packer = MessagePack.newDefaultPacker(new FileOutputStream(fileName, true));
        packer.packString("boop");
        packer.close();
        unpacker = MessagePack.newDefaultUnpacker(new FileInputStream(fileName));
        while (unpacker.hasNext()) {
            System.out.println(unpacker.unpackString());
        }
        unpacker.close();
    }
}
