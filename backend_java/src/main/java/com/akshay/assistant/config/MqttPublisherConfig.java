package com.akshay.assistant.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;

@Configuration 
public class MqttPublisherConfig {

    @Value("${mqtt.url}")
    private String url;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

       @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { url });
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public IntegrationFlow mqttOutboundFlow() {
           return IntegrationFlow.from("mqttChannel") // <--- Named input channel
            .handle(new MqttPahoMessageHandler( "someMqttClient", mqttClientFactory()))
            .get();
        }


}