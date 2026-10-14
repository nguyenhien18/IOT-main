package com.iotroom.mapper;

import org.springframework.stereotype.Component;

import com.iotroom.dto.response.SensorDataResponse;
import com.iotroom.entity.SensorData;

@Component
public class SensorDataMapper {
    public SensorDataResponse toSensorDataResponse(SensorData entity) {
        return new SensorDataResponse(
                entity.getId(),
                entity.getSensor() == null ? null : entity.getSensor().getId(),
                entity.getSensor() == null ? null : entity.getSensor().getName(),
                entity.getValue(),
                entity.getCreatedAt()
        );
    }
}

