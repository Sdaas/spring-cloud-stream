#!/bin/bash
echo "Starting a console producer .."
echo "Input must be in Key:Value format"
kafka-console-producer \
  --bootstrap-server 127.0.0.1:9092 \
  --topic test-topic \
  --property "parse.key=true" \
  --property "key.separator=:"