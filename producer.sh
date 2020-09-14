#!/bin/bash
echo "Starting a console producer .."
echo "Input must be in Key:Value format"
kafkacat -b localhost:9092 -P -t test-topic -K:
