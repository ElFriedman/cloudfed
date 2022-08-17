#!/bin/sh
java -jar target/cloudfed.jar experiment "0" 0 "type1:0:Exp[50]:Det[1]:Unif[1,1]|type2:1:Exp[50]:Det[1]:Unif[1,1]" "type1!50:1:0|type2!50:1:0" 10000000 1000000 "csv" 0 0.0005 0.05 100000