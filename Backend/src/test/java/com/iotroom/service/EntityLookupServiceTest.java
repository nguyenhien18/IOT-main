package com.iotroom.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.iotroom.entity.ActionHistory;
import com.iotroom.entity.Sensor;
import com.iotroom.exception.AppException;
import com.iotroom.repository.ActionHistoryRepository;
import com.iotroom.repository.SensorDataRepository;
import com.iotroom.repository.SensorRepository;

class EntityLookupServiceTest {

    @Test
    void sensorLookupReturnsSensorAndRejectsUnknownId() {
        SensorRepository sensorRepository = mock(SensorRepository.class);
        Sensor sensor = new Sensor();
        when(sensorRepository.findById(1L)).thenReturn(Optional.of(sensor));
        when(sensorRepository.findById(99L)).thenReturn(Optional.empty());
        SensorService service = new SensorService(sensorRepository, mock(SensorDataRepository.class));

        assertSame(sensor, service.getById(1L));
        assertThrows(AppException.class, () -> service.getById(99L));
    }

    @Test
    void actionLookupReturnsActionAndRejectsUnknownId() {
        ActionHistoryRepository repository = mock(ActionHistoryRepository.class);
        ActionHistory action = new ActionHistory();
        when(repository.findById(1L)).thenReturn(Optional.of(action));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        ActionHistoryService service = new ActionHistoryService(repository);

        assertSame(action, service.getById(1L));
        assertThrows(AppException.class, () -> service.getById(99L));
    }
}
