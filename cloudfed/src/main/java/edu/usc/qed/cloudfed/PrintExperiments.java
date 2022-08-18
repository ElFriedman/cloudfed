package edu.usc.qed.cloudfed;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PrintExperiments {
    public static void main (String [] args) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("instructions.sh"));
        bw.write("#!/bin/sh");
        bw.newLine();
        int runID = 0;
        for (int i = 0; i <= 50; i+=2) {
            for (int j =  0; j <= 50; j+=2) {
                String command = "java -jar target/cloudfed.jar experiment \""+runID+"\" "+runID+" \"type1:0:Exp[50]:Det[1]:Unif[1,1]|type2:1:Exp[50]:Det[1]:Unif[1,1]\" \"type1!50:1:"+i+"|type2!50:1:"+j+"\" 100000 300000 \"csv\" 0 0.00075 0.05 30000";
                bw.write(command);
                bw.newLine();
                runID += 1;
            }
        }
        bw.close();
    }
}
