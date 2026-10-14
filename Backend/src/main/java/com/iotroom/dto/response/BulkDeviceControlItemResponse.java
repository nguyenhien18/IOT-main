package com.iotroom.dto.response;

public record BulkDeviceControlItemResponse(
        Long deviceId,
        String deviceName,
        String action,
        String status,
        String deviceStatus,
        String message
) {
}
