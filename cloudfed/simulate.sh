#!/bin/sh
java -jar target/cloudfed.jar workload generate target/workload1.msgpack "Time[1000000]" -S "0" "type1:10:Exp[1.5]:Dist[0.7:1,0.3:2]:Unif[1,2]" "type2:7.5:Exp[2]:Det[1]:Unif[3,4]"

java -jar target/cloudfed.jar workload generate target/workload2.msgpack "Time[1000000]" --seed "1" "type3:8:Exp[2]:Dist[0.9:1,0.05:2,0.05:3]:Unif[2,3]"

java -jar target/cloudfed.jar workload merge target/workload.msgpack target/workload1.msgpack target/workload2.msgpack

java -jar target/cloudfed.jar workload info target/workload.msgpack -A

java -jar target/cloudfed.jar simulate simulate_unit_test.yaml output_unit.txt

java -jar target/cloudfed.jar metrics output_unit.txt -C