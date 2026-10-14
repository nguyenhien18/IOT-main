package com.iotroom.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import jakarta.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.iotroom.exception.AppException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttClientService implements MqttCallbackExtended {
    private final MqttProperties properties;
    private final ObjectMapper objectMapper;
    private final MqttInboundMessageService inboundMessageService;

    private volatile MqttClient client;

    @EventListener(ApplicationReadyEvent.class)
    public synchronized void start() {
        if (!properties.isEnabled()) {
            log.info("MQTT bridge is disabled");
            return;
        }

        try {
            if (client == null) {
                String uniqueClientId = properties.getClientId() + "-"
                        + UUID.randomUUID().toString().substring(0, 8);
                client = new MqttClient(properties.getBrokerUrl(), uniqueClientId, new MemoryPersistence());
                client.setCallback(this);
            }
            connectIfNeeded();
        } catch (MqttException exception) {
            log.warn("MQTT broker is not available at {}: {}",
                    properties.getBrokerUrl(), exception.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${app.mqtt.reconnect-delay-ms:5000}")
    public synchronized void reconnectIfNeeded() {
        if (!properties.isEnabled() || client == null || client.isConnected()) {
            return;
        }
        try {
            connectIfNeeded();
        } catch (MqttException exception) {
            log.debug("MQTT reconnect failed: {}", exception.getMessage());
        }
    }

    public void publishDeviceCommand(Long actionId, Long deviceId, String action) {
        MqttClient activeClient = client;
        if (!properties.isEnabled() || activeClient == null || !activeClient.isConnected()) {
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "MQTT broker is not connected");
        }

        try {
            byte[] payload = objectMapper.writeValueAsBytes(Map.of(
                    "actionId", actionId,
                    "deviceId", deviceId,
                    "action", action
            ));
            MqttMessage message = new MqttMessage(payload);
            message.setQos(properties.getQos());
            message.setRetained(false);
            activeClient.publish(properties.getControlTopic(), message);
        } catch (JacksonException | MqttException exception) {
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Cannot publish the device command through MQTT");
        }
    }

    public boolean isConnected() {
        MqttClient activeClient = client;
        return activeClient != null && activeClient.isConnected();
    }

    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
        try {
            client.subscribe(new String[]{
                    properties.getSensorTopic(),
                    properties.getFeedbackTopic()
            }, new int[]{properties.getQos(), properties.getQos()});
            log.info("MQTT connected to {} and subscribed to [{}, {}]",
                    serverUri, properties.getSensorTopic(), properties.getFeedbackTopic());
        } catch (MqttException exception) {
            log.error("Cannot subscribe to MQTT topics", exception);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost: {}", cause == null ? "unknown reason" : cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        try {
            if (properties.getSensorTopic().equals(topic)) {
                inboundMessageService.handleSensorData(payload);
            } else if (properties.getFeedbackTopic().equals(topic)) {
                inboundMessageService.handleDeviceFeedback(payload);
            }
        } catch (Exception exception) {
            log.error("Cannot process MQTT message on topic {}: {}", topic, payload, exception);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // No action is required after an outbound MQTT message is delivered.
    }

    @PreDestroy
    public synchronized void stop() {
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        } catch (MqttException exception) {
            log.debug("Error while closing MQTT client: {}", exception.getMessage());
        }
    }

    private void connectIfNeeded() throws MqttException {
        if (client.isConnected()) {
            return;
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(properties.getConnectionTimeoutSeconds());
        options.setKeepAliveInterval(20);

        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            options.setUserName(properties.getUsername());
        }
        if (properties.getPassword() != null && !properties.getPassword().isBlank()) {
            options.setPassword(properties.getPassword().toCharArray());
        }

        client.connect(options);
    }
}
