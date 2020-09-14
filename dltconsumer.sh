#!/bin/bash

# With Headers
#kafkacat -b localhost:9092 -t dlq-topic -C -f '===================\nKey: %k (%K bytes) Value: %s (%S bytes) Offset: %o\nHeaders: %h\n'

# Without Headers
kafkacat -b localhost:9092 -t dlq-topic -C -f 'Key: %k (%K bytes) Value: %s (%S bytes) Offset: %o\n'
