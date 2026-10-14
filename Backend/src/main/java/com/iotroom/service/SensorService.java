package com.iotroom.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iotroom.dto.response.LatestSensorsResponse;
import com.iotroom.entity.Sensor;
import com.iotroom.entity.SensorData;
import com.iotroom.exception.AppException;
import com.iotroom.repository.SensorDataRepository;
import com.iotroom.repository.SensorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SensorService {
    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;

    @Transactional(readOnly = true)
    public List<Sensor> getAll() {
        return sensorRepository.findAll().stream()
                .sorted(Comparator.comparing(Sensor::getId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Sensor getById(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Sensor not found"));
    }

    @Transactional(readOnly = true)
    public LatestSensorsResponse getLatest() {
        Double temperature = null;
        Double humidity = null;
        Double light = null;
        LocalDateTime latestTime = null;

        for (Sensor sensor : getAll()) {
            SensorData latest = sensorDataRepository.findTopBySensor_IdOrderByCreatedAtDesc(sensor.getId())
                    .orElse(null);
            if (latest == null) {
                continue;
            }

            String sensorName = sensor.getName().trim().toLowerCase(Locale.ROOT);
            switch (sensorName) {
                case "temperature" -> temperature = latest.getValue();
                case "humidity" -> humidity = latest.getValue();
                case "light" -> light = latest.getValue();
                default -> {
                }
            }

            if (latestTime == null || latest.getCreatedAt().isAfter(latestTime)) {
                latestTime = latest.getCreatedAt();
            }
        }

        return new LatestSensorsResponse(
                temperature,
                humidity,
                light,
                latestTime
        );
    }
}

