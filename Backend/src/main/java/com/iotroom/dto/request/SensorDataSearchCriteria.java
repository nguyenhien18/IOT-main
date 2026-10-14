package com.iotroom.dto.request;

import java.time.LocalDateTime;

public record SensorDataSearchCriteria(
        Long sensorId,
        String keyword,
        Double value,
        String time,
        LocalDateTime from,
        LocalDateTime to,
        String sort,
        Integer page,
        Integer size
) {
}
