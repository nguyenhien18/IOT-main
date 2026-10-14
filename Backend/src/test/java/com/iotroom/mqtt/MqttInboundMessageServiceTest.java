package com.iotroom.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iotroom.entity.ActionHistory;
import com.iotroom.entity.Device;
import com.iotroom.entity.Sensor;
import com.iotroom.entity.SensorData;
import com.iotroom.repository.ActionHistoryRepository;
import com.iotroom.repository.DeviceRepository;
import com.iotroom.repository.SensorDataRepository;
import com.iotroom.repository.SensorRepository;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class MqttInboundMessageServiceTest {
    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private SensorDataRepository sensorDataRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private ActionHistoryRepository actionHistoryRepository;

    private MqttCommandTracker commandTracker;
    private MqttInboundMessageService service;

    @BeforeEach
    void setUp() {
        commandTracker = new MqttCommandTracker();
        service = new MqttInboundMessageService(
                JsonMapper.builder().build(),
                sensorRepository,
                sensorDataRepository,
                deviceRepository,
                actionHistoryRepository,
                commandTracker
        );
    }

    @Test
    void storesAllSensorValuesFromEsp8266Payload() throws Exception {
        Sensor temperature = sensor(1L, "TEMPERATURE");
        Sensor humidity = sensor(2L, "HUMIDITY");
        Sensor light = sensor(3L, "LIGHT");
        when(sensorRepository.findByNameIgnoreCase("TEMPERATURE")).thenReturn(Optional.of(temperature));
        when(sensorRepository.findByNameIgnoreCase("HUMIDITY")).thenReturn(Optional.of(humidity));
        when(sensorRepository.findByNameIgnoreCase("LIGHT")).thenReturn(Optional.of(light));

        service.handleSensorData("{\"temp\":28.5,\"humid\":71.0,\"light\":640.5}");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SensorData>> captor = ArgumentCaptor.forClass(List.class);
        verify(sensorDataRepository).saveAll(captor.capture());
        List<SensorData> readings = captor.getValue();
        assertEquals(3, readings.size());
        assertEquals(List.of(28.5, 71.0, 640.5), readings.stream().map(SensorData::getValue).toList());
    }

    @Test
    void appliesSuccessfulDeviceFeedbackAndCompletesPendingCommand() throws Exception {
        Device device = new Device();
        device.setId(2L);
        device.setName("LED 2");
        device.setStatus(Device.Status.OFF);

        ActionHistory history = new ActionHistory();
        history.setId(42L);
        history.setDevice(device);
        history.setAction(ActionHistory.Action.ON);
        history.setStatus(ActionHistory.Status.PROCESSING);

        when(actionHistoryRepository.findById(42L)).thenReturn(Optional.of(history));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(actionHistoryRepository.save(any(ActionHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CompletableFuture<Boolean> completion = commandTracker.register(42L);

        service.handleDeviceFeedback(
                "{\"actionId\":42,\"deviceId\":2,\"status\":\"SUCCESS\",\"deviceStatus\":\"ON\"}"
        );

        assertEquals(Device.Status.ON, device.getStatus());
        assertEquals(ActionHistory.Status.SUCCESS, history.getStatus());
        assertTrue(completion.getNow(false));
    }

    private Sensor sensor(Long id, String name) {
        Sensor sensor = new Sensor();
        sensor.setId(id);
        sensor.setName(name);
        return sensor;
    }
}
