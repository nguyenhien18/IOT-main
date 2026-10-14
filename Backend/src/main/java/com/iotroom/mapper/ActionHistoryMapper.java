package com.iotroom.mapper;

import org.springframework.stereotype.Component;

import com.iotroom.dto.response.ActionHistoryResponse;
import com.iotroom.dto.response.DeviceControlResponse;
import com.iotroom.entity.ActionHistory;

@Component
public class ActionHistoryMapper {
    public ActionHistoryResponse toActionHistoryResponse(ActionHistory entity) {
        return new ActionHistoryResponse(
                entity.getId(),
                entity.getUser() == null ? null : entity.getUser().getId(),
                entity.getUser() == null ? null : entity.getUser().getUsername(),
                entity.getDevice() == null ? null : entity.getDevice().getId(),
                entity.getDevice() == null ? null : entity.getDevice().getName(),
                entity.getAction() == null ? null : entity.getAction().name(),
                entity.getStatus() == null ? null : entity.getStatus().name(),
                entity.getCreatedAt()
        );
    }

    public DeviceControlResponse toDeviceControlResponse(ActionHistory entity) {
        return new DeviceControlResponse(
                entity.getId(),
                entity.getUser() == null ? null : entity.getUser().getId(),
                entity.getUser() == null ? null : entity.getUser().getUsername(),
                entity.getDevice() == null ? null : entity.getDevice().getId(),
                entity.getDevice() == null ? null : entity.getDevice().getName(),
                entity.getAction() == null ? null : entity.getAction().name(),
                entity.getStatus() == null ? null : entity.getStatus().name(),
                entity.getDevice() == null || entity.getDevice().getStatus() == null
                        ? null
                        : entity.getDevice().getStatus().name(),
                entity.getCreatedAt()
        );
    }
}
