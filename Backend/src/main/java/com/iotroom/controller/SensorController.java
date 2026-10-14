package com.iotroom.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iotroom.dto.response.ApiResponse;
import com.iotroom.dto.response.LatestSensorsResponse;
import com.iotroom.dto.response.SensorResponse;
import com.iotroom.mapper.SensorMapper;
import com.iotroom.service.SensorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {
    private final SensorService sensorService;
    private final SensorMapper sensorMapper;

    @GetMapping
    public ApiResponse<List<SensorResponse>> getAll() {
        return ApiResponse.ok(sensorService.getAll().stream()
                .map(sensorMapper::toSensorResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<SensorResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(sensorMapper.toSensorResponse(sensorService.getById(id)));
    }

    @GetMapping("/latest")
    public ApiResponse<LatestSensorsResponse> getLatest() {
        return ApiResponse.ok(sensorService.getLatest());
    }
}

