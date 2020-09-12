package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;


@Slf4j
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public ListenerContainerCustomizer<AbstractMessageListenerContainer<?, ?>> cust() {
		log.info("customizer() called");

		return (container, destination, group) -> {

			//Logging the error
			// container.setErrorHandler(new MyLoggingErrorHandler());

			// Setting a new SeekToCurrentErrorHandler
			BackOff backoff = new FixedBackOff(0L, 0L);
			SeekToCurrentErrorHandler errorHandler = new SeekToCurrentErrorHandler((record, exception) -> {}, backoff);
			container.setErrorHandler(errorHandler);
		};
	}
}