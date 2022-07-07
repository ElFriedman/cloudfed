#!/bin/sh
java -jar target/cloudfed.jar workload generate target/workload.msgpack "Time[1000]" "type1:0:Exp[50]:Det[1]:Unif[1,1]" "type2:0:Exp[50]:Det[1]:Unif[1,1]"

java -jar target/cloudfed.jar workload info target/workload.msgpack -A

java -jar target/cloudfed.jar simulate sim_E2E1.yaml output_E2E1.txt

java -jar target/cloudfed.jar metrics output_E2E1.txt