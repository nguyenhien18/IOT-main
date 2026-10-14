package com.iotroom.dto.response;

import java.time.LocalDateTime;

public record DeviceResponse(
        Long id,
        String name,
        String status,
        LocalDateTime createdAt
) {
}

