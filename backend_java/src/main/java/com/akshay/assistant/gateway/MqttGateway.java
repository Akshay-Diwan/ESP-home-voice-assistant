package com.akshay.assistant.gateway;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway (defaultRequestChannel = "mqttChannel")
public interface MqttGateway {
    // Sends the payload to the MQTT topic specified by the header
    void sendToMqtt(String payload, @Header ("mqtt_topic") String topic);
}


