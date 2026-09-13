package com.akshay.assistant.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttPublisherConfig {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.topic}")
    private String topic;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {

        DefaultMqttPahoClientFactory factory =
                new DefaultMqttPahoClientFactory();

        MqttConnectOptions options =
                new MqttConnectOptions();

        options.setServerURIs(new String[]{broker});
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        factory.setConnectionOptions(options);

        return factory;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageHandler mqttOutbound(
            MqttPahoClientFactory mqttClientFactory) {

        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(
                        clientId,
                        mqttClientFactory
                );

        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(topic);
        messageHandler.setDefaultQos(1);

        return messageHandler;
    }

    @Bean
    public IntegrationFlow mqttOutboundFlow(
            MessageChannel mqttOutboundChannel,
            MessageHandler mqttOutbound) {

        return IntegrationFlow
                .from(mqttOutboundChannel)
                .handle(mqttOutbound)
                .get();
    }
}