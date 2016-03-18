#!/bin/bash
vertx=$(vertx list | awk '{ print $1 }' | grep -v Listing)

for i in $vertx
do 
	vertx stop $i
done
