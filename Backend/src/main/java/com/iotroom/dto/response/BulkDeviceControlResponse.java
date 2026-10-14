package com.iotroom.dto.response;

import java.util.List;

public record BulkDeviceControlResponse(
        String action,
        int total,
        int successCount,
        int failedCount,
        List<BulkDeviceControlItemResponse> devices
) {
}
