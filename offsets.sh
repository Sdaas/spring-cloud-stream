#!/bin/bash
echo "infinite loops [ hit CTRL+C to stop]"
while :
do
  kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --describe --group group1
	sleep 5
done