package com.iotroom.dto.response;

import java.time.LocalDateTime;

public record SensorDataResponse(
        Long id,
        Long sensorId,
        String sensorName,
        Double value,
        LocalDateTime createdAt
) {
}

