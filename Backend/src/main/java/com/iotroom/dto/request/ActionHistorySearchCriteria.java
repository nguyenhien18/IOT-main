package com.iotroom.dto.request;

import java.time.LocalDateTime;

public record ActionHistorySearchCriteria(
        Long deviceId,
        String keyword,
        String action,
        String status,
        LocalDateTime from,
        LocalDateTime to,
        String sort,
        Integer page,
        Integer size
) {
}
