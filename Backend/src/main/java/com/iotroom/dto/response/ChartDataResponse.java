package com.iotroom.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartDataResponse {
    private LocalDateTime time;
    private Double temperature;
    private Double humidity;
    private Double light;
}

