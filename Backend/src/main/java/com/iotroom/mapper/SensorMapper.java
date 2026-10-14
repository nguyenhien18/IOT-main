package com.iotroom.mapper;

import org.springframework.stereotype.Component;

import com.iotroom.dto.response.SensorResponse;
import com.iotroom.entity.Sensor;

@Component
public class SensorMapper {
    public SensorResponse toSensorResponse(Sensor entity) {
        return new SensorResponse(
                entity.getId(),
                entity.getName(),
                entity.getUnit(),
                entity.getCreatedAt()
        );
    }
}

