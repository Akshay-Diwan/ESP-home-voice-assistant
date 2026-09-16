// package com.akshay.assistant;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.integration.annotation.IntegrationComponentScan;
// import org.springframework.integration.config.EnableIntegration;

// @SpringBootTest(properties = {
//     "mqtt.url=tcp://localhost:1883",
//     "mqtt.username=user1",
//     "mqtt.password=akshay"
// }) 
// @EnableIntegration
// @IntegrationComponentScan(basePackages = "com.akshay.assistant.gateway")
// public class MqttPublisherTest {
//      @Autowired 
//      private MqttPublisher publisher;
    
//      @Test()
//      void publish()throws InterruptedException{
//          publisher.publish("Hello i am spring");
//          Thread.sleep(2000); 
//      }
//  }
