#!/bin/bash
vertx=$(java -jar target/clique-1.0-SNAPSHOT-fat.jar list | awk '{ print $1 }' | grep -v Listing)

for i in $vertx
do 
	java -jar target/clique-1.0-SNAPSHOT-fat.jar stop $i
done
