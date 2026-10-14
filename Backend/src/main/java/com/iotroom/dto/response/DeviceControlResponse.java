package com.iotroom.dto.response;

import java.time.LocalDateTime;

public record DeviceControlResponse(
        Long id,
        Long userId,
        String username,
        Long deviceId,
        String deviceName,
        String action,
        String status,
        String deviceStatus,
        LocalDateTime createdAt
) {
}
