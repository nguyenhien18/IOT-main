package com.iotroom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.iotroom.dto.response.BulkDeviceControlResponse;
import com.iotroom.entity.ActionHistory;
import com.iotroom.entity.Device;
import com.iotroom.exception.AppException;
import com.iotroom.mqtt.MqttClientService;
import com.iotroom.mqtt.MqttCommandTracker;
import com.iotroom.mqtt.MqttProperties;
import com.iotroom.repository.ActionHistoryRepository;
import com.iotroom.repository.DeviceRepository;

class DeviceServiceTest {

    @Test
    void controlAllReturnsSuccessAndFailureForEachDevice() {
        DeviceRepository deviceRepository = mock(DeviceRepository.class);
        ActionHistoryRepository actionHistoryRepository = mock(ActionHistoryRepository.class);
        Device first = device(1L, "LED 1", Device.Status.ON);
        Device second = device(2L, "LED 2", Device.Status.OFF);
        when(deviceRepository.findAll()).thenReturn(List.of(first, second));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(first));
        when(deviceRepository.findById(2L)).thenReturn(Optional.of(second));

        DeviceService service = spy(new DeviceService(
                deviceRepository,
                actionHistoryRepository,
                mock(ProfileService.class),
                mock(MqttClientService.class),
                mock(MqttCommandTracker.class),
                mock(MqttProperties.class)
        ));

        ActionHistory success = new ActionHistory();
        success.setDevice(first);
        success.setAction(ActionHistory.Action.ON);
        success.setStatus(ActionHistory.Status.SUCCESS);
        success.setCreatedAt(LocalDateTime.now());
        doReturn(success).when(service).control(1L, "ON");
        doThrow(new AppException(HttpStatus.GATEWAY_TIMEOUT, "ESP8266 timeout"))
                .when(service).control(2L, "ON");

        BulkDeviceControlResponse result = service.controlAll("on");

        assertEquals("ON", result.action());
        assertEquals(2, result.total());
        assertEquals(1, result.successCount());
        assertEquals(1, result.failedCount());
        assertEquals("SUCCESS", result.devices().get(0).status());
        assertEquals("FAILED", result.devices().get(1).status());
        assertEquals("ESP8266 timeout", result.devices().get(1).message());
    }

    private Device device(Long id, String name, Device.Status status) {
        Device device = new Device();
        device.setId(id);
        device.setName(name);
        device.setStatus(status);
        device.setCreatedAt(LocalDateTime.now());
        return device;
    }
}
