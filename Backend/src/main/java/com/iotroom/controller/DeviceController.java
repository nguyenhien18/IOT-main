package com.iotroom.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iotroom.dto.request.ControlDeviceRequest;
import com.iotroom.dto.response.ApiResponse;
import com.iotroom.dto.response.BulkDeviceControlResponse;
import com.iotroom.dto.response.DeviceControlResponse;
import com.iotroom.dto.response.DeviceResponse;
import com.iotroom.mapper.ActionHistoryMapper;
import com.iotroom.mapper.DeviceMapper;
import com.iotroom.service.DeviceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;
    private final ActionHistoryMapper actionHistoryMapper;

    @GetMapping
    public ApiResponse<List<DeviceResponse>> getAll() {
        return ApiResponse.ok(deviceService.getAll().stream()
                .map(deviceMapper::toDeviceResponse)
                .toList());
    }

    @PostMapping("/{id}/control")
    public ApiResponse<DeviceControlResponse> control(
            @PathVariable Long id,
            @Valid @RequestBody ControlDeviceRequest request
    ) {
        return ApiResponse.ok(
                actionHistoryMapper.toDeviceControlResponse(deviceService.control(id, request.getAction()))
        );
    }

    @PostMapping("/control-all")
    public ApiResponse<BulkDeviceControlResponse> controlAll(
            @Valid @RequestBody ControlDeviceRequest request
    ) {
        BulkDeviceControlResponse result = deviceService.controlAll(request.getAction());
        String message = result.failedCount() == 0 ? "success" : "completed with failures";
        return ApiResponse.ok(message, result);
    }
}

