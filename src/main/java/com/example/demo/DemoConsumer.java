package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Map;

@EnableBinding(Sink.class)
@Slf4j
public class DemoConsumer {

    @StreamListener(Sink.INPUT)
    public void handle(String msg){
        log.info("DemoConsumer::handle() was called");
        log.info(msg);

        throw new ProcessingException();
    }

    /*
    @StreamListener(Sink.INPUT)
    public void logger(@Payload  String payload, @Headers Map<String,Object> headers){
        log.info("DemoConsumer::logger called");
        log.info("Payload : {}", payload);
        log.info("Headers ...");
        for( String key : headers.keySet()) {
            log.info("{}: {}", key, headers.get(key));
        }
    }

     */
}
