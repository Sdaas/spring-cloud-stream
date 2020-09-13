
## Introduction

Work in Progress

## Retry Approaches

In the [Error Handling](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.6.RELEASE/reference/html/spring-cloud-stream.html#spring-cloud-stream-overview-error-handling)
section of the Spring Cloud Stream document, it says that when the application throws an exception, the Spring `RetryTemplate` is used to retry. If that fails, 
then it is sent to the container's error handler in the binder's container, where the `SeekToCurrentErrorHandler` will also try multiple times before sending it to a
dead-letter topic. Think of these as two nested loops. This suggests two approaches

### Use `RetryTemplate` to retry

Use this approach if ALL the retries can be completed within `max.poll.interval.ms` (typically 5 mins) of the consumer. 

Configure the retry at the spring-cloud-stream level as 

```yaml
consumer:
  maxAttempts : 5
  backOffInitialInterval : 10000 # 10seconds
  backoffMultiplier : 2.0
```
would allow a maximum of four retries before the total time exceeds the default 5mins of `max.poll.interval.ms` and triggers a consumer 
rebalance. To avoid that, the `maxAttempts` should be set to 5000 or less. The `SeekToCurrentErrorHandler` should be configured with 
a `FixedBackoff(0,0)` so that it does not attempt any retries and sends the data directly to the dead-letter-topic.

### Use `SeektoCurrentErrorHandler` to retry

With this approach there is no upper limit on the TOTAL amount of time spent on retries. But the delay between INDIVIDUAL retries
cannot exceed `max.poll.interval.ms`. 

Disable the retry in the spring-cloud-stream level by setting `spring.cloud.stream.bindings.xxxx.consumer.maxAttempts=1` . 
Configure the `SeekToCurrentErrorHandler` to use a [FixedBackOff](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/util/backoff/FixedBackOff.html)
or [ExponentialBackoff](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/util/backoff/ExponentialBackOff.html).
 
If using ExponentialBackoff, makes sure to

* Set the `maxInterval` to a value less than `max.poll.interval.ms`
* Set `maxElapsedTime` to a reasonable value. This has a default of `Long.MAX_VALUE` which would lead to infinite number of retries

For example, using

```
long initialInterval = 5000;	// initial interval in milliseconds
double multiplier = 2.0;		// so retries will be done after 5, 10, 20, 40, 60 second interval
long maxInterval = 30 * 1000L;  // maximum wait time between two retries. This will hence cap it to 5, 10, 20, 30, 30
long maxElapsedTime = 80 * 1000L;  // no retry will be done after this much time has elapsed
```
will lead to a maximum of 5 retries (i.e. 6 attempts) with a pause of 5, 10, 20, 30, and 30 seconds between pauses.

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

### SeekToCurrentErrorHandler

The default `SeekToCurrentErrorHandler` in the listener's container is configured for 10 attempts with no waiting time in between (ie.,interval=0, maxAttempts=10 )

This backoff behavior and the recovery action (what to do if all retries fails) can be configured using `ListenerContainerCustomizer` as shown below
```
    @Autowired
	ProducerFactory<?,?> producerFactory;

	@Bean
	public ListenerContainerCustomizer<AbstractMessageListenerContainer<?, ?>> customizer() {

		return (container, destination, group) -> {
			BackOff backoff = ... some code to return a Backoff object ....
			KafkaTemplate<String,String> template = new KafkaTemplate<>((ProducerFactory<String, String>) producerFactory);
			DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
					(KafkaOperations) template,
					(rec, ex) -> new TopicPartition("dlq-topic",0));
			SeekToCurrentErrorHandler errorHandler = new SeekToCurrentErrorHandler(recoverer, backoff);
			container.setErrorHandler(errorHandler);
		};
	}
```
## References

* [Spring Cloud Stream 3.0.x](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.6.RELEASE/reference/html/spring-cloud-stream.html#spring-cloud-stream-reference)
    * [Retry Template and Backoff](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.6.RELEASE/reference/html/spring-cloud-stream.html#_retry_template_and_retrybackoff)
* [Apache Kafka Binder](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream-binder-kafka/3.0.6.RELEASE/reference/html/spring-cloud-stream-binder-kafka.html)
    * [Consumer Properties](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream-binder-kafka/3.0.6.RELEASE/reference/html/spring-cloud-stream-binder-kafka.html#kafka-consumer-properties)
    * [Dead Letter Queue processing](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream-binder-kafka/3.0.6.RELEASE/reference/html/spring-cloud-stream-binder-kafka.html#kafka-dlq-processing)