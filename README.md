

## RetryTemplate

See the Spring Cloud Stream documentation on [Retry Template and Backoff](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.6.RELEASE/reference/html/spring-cloud-stream.html#_retry_template_and_retrybackoff)

```yaml
spring:
  cloud:
    stream:
      bindings:
        input:
          binder: kafka
          destination: test-topic
          consumer:
            maxAttempts: 4
            backOffInitialInterval : 10000 # 10seconds
            backOffMaxInterval : 100000    # 100 seconds
            backoffMultiplier : 2.0       # retries will be done after 10 20 40 80 seconds. The total is 150 which is within the default max retry interval
```
 
```
2020-09-12 14:57:13.654  INFO 22220 --- [container-0-C-1] com.example.demo.DemoConsumer            : DemoConsumer::handle() was called
2020-09-12 14:57:23.660  INFO 22220 --- [container-0-C-1] com.example.demo.DemoConsumer            : DemoConsumer::handle() was called
2020-09-12 14:57:43.665  INFO 22220 --- [container-0-C-1] com.example.demo.DemoConsumer            : DemoConsumer::handle() was called
2020-09-12 14:58:23.671  INFO 22220 --- [container-0-C-1] com.example.demo.DemoConsumer            : DemoConsumer::handle() was called
2020-09-12 14:58:23.675 ERROR 22220 --- [container-0-C-1] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.MessagingException: 
    Exception thrown while invoking DemoConsumer#handle[1 args]; 
	at org.springframework.cloud.stream.binding.StreamListenerMessageHandler.handleRequestMessage(StreamListenerMessageHandler.java:64)
	at org.springframework.integr
```

## References

* [Spring Cloud Stream 3.0.x](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.6.RELEASE/reference/html/spring-cloud-stream.html#spring-cloud-stream-reference)
    * [Retry Template and Backoff](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.6.RELEASE/reference/html/spring-cloud-stream.html#_retry_template_and_retrybackoff)
    