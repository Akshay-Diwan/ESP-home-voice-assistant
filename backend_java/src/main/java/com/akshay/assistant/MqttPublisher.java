package com.akshay.assistant;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public class MqttPublisher {

    private final MessageChannel mqttOutboundChannel;

    public MqttPublisher(MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    public void publish(String message) {
        mqttOutboundChannel.send(
            MessageBuilder.withPayload(message).build()
        );
    }
}