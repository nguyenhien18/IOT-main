package com.iotroom.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iotroom.dto.request.CreateSensorDataRequest;
import com.iotroom.dto.request.SensorDataSearchCriteria;
import com.iotroom.dto.response.ApiResponse;
import com.iotroom.dto.response.ChartDataResponse;
import com.iotroom.dto.response.PageResponse;
import com.iotroom.dto.response.SensorDataResponse;
import com.iotroom.mapper.SensorDataMapper;
import com.iotroom.service.SensorDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/data-sensors")
@RequiredArgsConstructor
public class SensorDataController {
    private final SensorDataService sensorDataService;
    private final SensorDataMapper sensorDataMapper;

    @GetMapping
    public ApiResponse<PageResponse<SensorDataResponse>> getAll(
            @RequestParam(required = false) Long sensorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double value,
            @RequestParam(required = false) String time,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        SensorDataSearchCriteria criteria = new SensorDataSearchCriteria(
                sensorId, keyword, value, time, from, to, sort, page, size
        );
        return ApiResponse.ok(PageResponse.from(
                sensorDataService.search(criteria)
                        .map(sensorDataMapper::toSensorDataResponse)
        ));
    }

    @GetMapping("/chart")
    public ApiResponse<List<ChartDataResponse>> getChartData() {
        return ApiResponse.ok(sensorDataService.getChartData());
    }

    @PostMapping
    public ApiResponse<SensorDataResponse> create(@Valid @RequestBody CreateSensorDataRequest request) {
        return ApiResponse.ok(sensorDataMapper.toSensorDataResponse(sensorDataService.create(request)));
    }
}

