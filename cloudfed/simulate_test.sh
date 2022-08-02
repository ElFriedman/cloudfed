#!/bin/sh
java -jar target/cloudfed.jar workload generate target/workload.msgpack "Time[10000]" "type1:0:Exp[50]:Det[1]:Unif[1,2]" "type2:0:Exp[50]:Det[1]:Unif[1,1]"

java -jar target/cloudfed.jar workload info target/workload.msgpack -A

java -jar target/cloudfed.jar simulate simulate_test.yaml output_test.txt

java -jar target/cloudfed.jar metrics output_test.txt 100000 -B 40000 -C "50000:6000"