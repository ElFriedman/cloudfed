#!/bin/sh
java -jar target/cloudfed.jar workload generate target/workload.msgpack "Job[100000010]" "type1:0:Exp[50]:Det[1]:Unif[1,1]" "type2:0:Exp[70]:Det[1]:Unif[1,1]"

java -jar target/cloudfed.jar workload info target/workload.msgpack -A

java -jar target/cloudfed.jar simulate simulate_test.yaml target/output_test.txt

java -jar target/cloudfed.jar metrics target/output_test.txt 1000000 -B 150000