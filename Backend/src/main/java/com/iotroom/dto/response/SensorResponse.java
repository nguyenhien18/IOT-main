package com.iotroom.dto.response;

import java.time.LocalDateTime;

public record SensorResponse(
        Long id,
        String name,
        String unit,
        LocalDateTime createdAt
) {
}

