package com.iotroom.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mqtt")
public class MqttProperties {
    private boolean enabled = true;
    private String brokerUrl = "tcp://localhost:1885";
    private String clientId = "iot-room-backend";
    private String username = "";
    private String password = "";
    private String sensorTopic = "sensor_data";
    private String controlTopic = "device_control";
    private String feedbackTopic = "device_feedback";
    private int qos = 1;
    private int connectionTimeoutSeconds = 3;
    private long reconnectDelayMs = 5000;
    private long commandTimeoutMs = 5000;
}
