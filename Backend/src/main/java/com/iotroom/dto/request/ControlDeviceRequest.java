package com.iotroom.dto.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
public class ControlDeviceRequest {
    @NotBlank(message = "Action must not be blank")
    @Schema(description = "Trang thai muon dieu khien", example = "ON", allowableValues = {"ON", "OFF"})
    private String action;
}
