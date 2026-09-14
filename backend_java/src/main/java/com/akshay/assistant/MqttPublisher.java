package com.akshay.assistant;
import org.springframework.stereotype.Component;

import com.akshay.assistant.gateway.MqttGateway;

@Component
public class MqttPublisher {
    private final MqttGateway mqttGateway;
    public MqttPublisher(MqttGateway mqttGateway){
        this.mqttGateway = mqttGateway;
    }
    public void publish(String message) {
        mqttGateway.sendToMqtt(message, "test/topic");
    }
}