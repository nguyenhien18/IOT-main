package com.iotroom.dto.response;

import java.time.LocalDateTime;

public record LatestSensorsResponse(
        Double temperature,
        Double humidity,
        Double light,
        LocalDateTime time
) {
}

