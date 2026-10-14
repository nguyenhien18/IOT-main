package com.iotroom.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
public class CreateSensorDataRequest {
    @NotNull(message = "Sensor ID must not be null")
    @Schema(example = "1")
    private Long sensorId;

    @NotNull(message = "Value must not be null")
    @Schema(example = "27.5")
    private Double value;

    @Schema(description = "Bo trong de backend dung thoi gian hien tai", example = "2026-09-14T23:00:00")
    private LocalDateTime createdAt;
}
