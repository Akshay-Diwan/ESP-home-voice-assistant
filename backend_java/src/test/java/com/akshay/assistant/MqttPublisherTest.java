package com.akshay.assistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest 
public class MqttPublisherTest {
    
    @Autowired 
    private MqttPublisher publisher;
    
    @Test()
    void publish(){
        publisher.publish("Hello i am spring");
    }
}
