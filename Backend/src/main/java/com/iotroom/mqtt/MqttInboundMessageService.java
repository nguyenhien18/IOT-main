package com.iotroom.mqtt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.iotroom.entity.ActionHistory;
import com.iotroom.entity.Device;
import com.iotroom.entity.Sensor;
import com.iotroom.entity.SensorData;
import com.iotroom.repository.ActionHistoryRepository;
import com.iotroom.repository.DeviceRepository;
import com.iotroom.repository.SensorDataRepository;
import com.iotroom.repository.SensorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttInboundMessageService {
    private final ObjectMapper objectMapper;
    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;
    private final DeviceRepository deviceRepository;
    private final ActionHistoryRepository actionHistoryRepository;
    private final MqttCommandTracker commandTracker;

    @Transactional
    public void handleSensorData(String payload) throws JacksonException {
        SensorTelemetryMessage message = objectMapper.readValue(payload, SensorTelemetryMessage.class);
        LocalDateTime measuredAt = LocalDateTime.now().withNano(0);
        List<SensorData> readings = new ArrayList<>();

        addReading(readings, "TEMPERATURE", message.temp(), measuredAt);
        addReading(readings, "HUMIDITY", message.humid(), measuredAt);
        addReading(readings, "LIGHT", message.light(), measuredAt);

        if (readings.isEmpty()) {
            throw new IllegalArgumentException("MQTT sensor payload contains no valid readings");
        }

        sensorDataRepository.saveAll(readings);
        log.debug("Stored {} MQTT sensor readings", readings.size());
    }

    @Transactional
    public void handleDeviceFeedback(String payload) throws JacksonException {
        DeviceFeedbackMessage message = objectMapper.readValue(payload, DeviceFeedbackMessage.class);

        if (message.devices() != null && !message.devices().isEmpty()) {
            synchronizeDeviceStates(message.devices());
        }

        if (message.actionId() == null || message.deviceId() == null) {
            return;
        }

        ActionHistory history = actionHistoryRepository.findById(message.actionId()).orElse(null);
        if (history == null
                || history.getStatus() != ActionHistory.Status.PROCESSING
                || !history.getDevice().getId().equals(message.deviceId())) {
            log.warn("Ignoring unmatched MQTT feedback for action {} and device {}",
                    message.actionId(), message.deviceId());
            return;
        }

        boolean success = "SUCCESS".equalsIgnoreCase(message.status());
        if (success) {
            Device.Status finalStatus = parseDeviceStatus(message.deviceStatus(), history.getAction());
            history.getDevice().setStatus(finalStatus);
            deviceRepository.save(history.getDevice());
            history.setStatus(ActionHistory.Status.SUCCESS);
        } else {
            history.setStatus(ActionHistory.Status.FAILED);
        }

        actionHistoryRepository.save(history);
        completeAfterCommit(message.actionId(), success);
    }

    private void addReading(
            List<SensorData> readings,
            String sensorName,
            Double value,
            LocalDateTime measuredAt
    ) {
        if (value == null || !Double.isFinite(value)) {
            return;
        }

        Sensor sensor = sensorRepository.findByNameIgnoreCase(sensorName)
                .orElseThrow(() -> new IllegalStateException("Sensor is not registered: " + sensorName));
        SensorData reading = new SensorData();
        reading.setSensor(sensor);
        reading.setValue(value);
        reading.setCreatedAt(measuredAt);
        readings.add(reading);
    }

    private void synchronizeDeviceStates(List<DeviceStateMessage> states) {
        for (DeviceStateMessage state : states) {
            if (state.deviceId() == null || state.status() == null) {
                continue;
            }
            deviceRepository.findById(state.deviceId()).ifPresent(device -> {
                try {
                    device.setStatus(Device.Status.valueOf(state.status().trim().toUpperCase(Locale.ROOT)));
                    deviceRepository.save(device);
                } catch (IllegalArgumentException exception) {
                    log.warn("Ignoring invalid state '{}' for device {}", state.status(), state.deviceId());
                }
            });
        }
    }

    private Device.Status parseDeviceStatus(String rawStatus, ActionHistory.Action fallbackAction) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return Device.Status.valueOf(fallbackAction.name());
        }
        return Device.Status.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
    }

    private void completeAfterCommit(Long actionId, boolean success) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            commandTracker.complete(actionId, success);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                commandTracker.complete(actionId, success);
            }
        });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SensorTelemetryMessage(
            Double temp,
            @JsonAlias("humidity") Double humid,
            Double light
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DeviceFeedbackMessage(
            Long actionId,
            Long deviceId,
            String status,
            String deviceStatus,
            List<DeviceStateMessage> devices
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DeviceStateMessage(Long deviceId, String status) {
    }
}
