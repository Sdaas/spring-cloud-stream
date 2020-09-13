package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;


@Slf4j
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	ProducerFactory<?,?> producerFactory;

	private BackOff getBackoff(){

		// long interval = 5000; // interval between retries in milliseconds
		// long maxAttempts = 5; // maximmum number of attempts
		// return new FixedBackOff(interval, maxAttempts);

		long initialInterval = 5000;	// initial interval in milliseconds
		double multiplier = 2.0;		// so retries will be done after 5, 10, 20, 40, 60 second interval
		long maxInterval = 30 * 1000L;  // maximum wait time between two retries. This will hence cap it to 5, 10, 20, 30, 30
		long maxElapsedTime = 80 * 1000L;  // no retry will be done after this much time has elapsed

		ExponentialBackOff backoff = new ExponentialBackOff(initialInterval, multiplier);
		backoff.setMaxInterval(maxInterval);
		backoff.setMaxElapsedTime(maxElapsedTime);
		return backoff;

		//return new FixedBackOff(0L,0L);
	}

	/*
	Dont use this if using configured dlq

	@Bean
	public ListenerContainerCustomizer<AbstractMessageListenerContainer<?, ?>> customizer() {
		log.info("customizer() called");

		return (container, destination, group) -> {

			// container is a AbstractMessageListenerContainer
			// https://docs.spring.io/spring-kafka/docs/2.6.0/api/org/springframework/kafka/listener/AbstractMessageListenerContainer.html

			//Logging the error
			//container.setErrorHandler(new MyLoggingErrorHandler());

			// Setting a new SeekToCurrentErrorHandler. For this we will specify a backoff policy, and a "recoverer"
			// that will be executed if all the retry fails
			BackOff backoff = getBackoff();
			KafkaTemplate<String,String> template = new KafkaTemplate<>((ProducerFactory<String, String>) producerFactory);
			DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
					(KafkaOperations) template,
					(rec, ex) -> new TopicPartition("dlq-topic",0));
			SeekToCurrentErrorHandler errorHandler = new SeekToCurrentErrorHandler(recoverer, backoff);
			container.setErrorHandler(errorHandler);


		};
	}
	 */
}