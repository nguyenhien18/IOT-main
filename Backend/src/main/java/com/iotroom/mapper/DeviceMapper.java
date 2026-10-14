package com.iotroom.mapper;

import org.springframework.stereotype.Component;

import com.iotroom.dto.response.DeviceResponse;
import com.iotroom.entity.Device;

@Component
public class DeviceMapper {
    public DeviceResponse toDeviceResponse(Device entity) {
        return new DeviceResponse(
                entity.getId(),
                entity.getName(),
                entity.getStatus() == null ? null : entity.getStatus().name(),
                entity.getCreatedAt()
        );
    }
}

