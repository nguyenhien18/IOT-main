package com.iotroom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.iotroom.entity.Sensor;
import com.iotroom.entity.SensorData;
import com.iotroom.repository.SensorDataRepository;
import com.iotroom.repository.SensorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sensor-simulation.enabled", havingValue = "true")
public class SensorSimulationService {
    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;

    @Scheduled(fixedRateString = "${app.sensor-simulation.interval-ms:5000}")
    public void generateReadings() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<SensorData> rows = sensorRepository.findAll().stream()
                .map(sensor -> createReading(sensor, now))
                .toList();
        sensorDataRepository.saveAll(rows);
    }

    private SensorData createReading(Sensor sensor, LocalDateTime createdAt) {
        double value = switch (sensor.getName().trim().toLowerCase(Locale.ROOT)) {
            case "temperature" -> random(27.0, 32.0);
            case "humidity" -> random(60.0, 78.0);
            case "light" -> random(450.0, 700.0);
            default -> random(0.0, 100.0);
        };

        SensorData sensorData = new SensorData();
        sensorData.setSensor(sensor);
        sensorData.setValue(Math.round(value * 10.0) / 10.0);
        sensorData.setCreatedAt(createdAt);
        return sensorData;
    }

    private double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}

