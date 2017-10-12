#!/bin/bash

num_process=`ps -ef | grep "run MSB.java" | wc -l`
while [ "$num_process" -gt 1 ]; do
	pid=`ps -ef | grep "run MSB.java" | head -1 | tr -s ' ' | cut -d ' ' -f2`
	sudo kill -9 $pid
	num_process=`ps -ef | grep "run MSB.java" | wc -l`
done
  
sudo nohup /home/ubuntu/vertx/bin/vertx -Xmx1m run MSB.java & > /dev/null
