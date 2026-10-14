package com.iotroom.dto.response;

import java.time.LocalDateTime;

public record ActionHistoryResponse(
        Long id,
        Long userId,
        String username,
        Long deviceId,
        String deviceName,
        String action,
        String status,
        LocalDateTime createdAt
) {
}
