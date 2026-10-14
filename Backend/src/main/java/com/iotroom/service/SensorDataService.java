package com.iotroom.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.text.Normalizer;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iotroom.dto.request.CreateSensorDataRequest;
import com.iotroom.dto.request.SensorDataSearchCriteria;
import com.iotroom.dto.response.ChartDataResponse;
import com.iotroom.entity.Sensor;
import com.iotroom.entity.SensorData;
import com.iotroom.exception.AppException;
import com.iotroom.repository.SensorDataRepository;
import com.iotroom.repository.SensorRepository;
import com.iotroom.util.DateTimeSearchUtils;
import com.iotroom.util.DateTimeSearchUtils.DateTimeParts;
import com.iotroom.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SensorDataService {
    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "value", "id");

    private final SensorDataRepository sensorDataRepository;
    private final SensorRepository sensorRepository;

    @Transactional(readOnly = true)
    public Page<SensorData> search(SensorDataSearchCriteria criteria) {
        Long sensorId = criteria.sensorId();
        String rawKeyword = criteria.keyword();
        Double value = criteria.value();
        String rawTime = criteria.time();
        LocalDateTime from = criteria.from();
        LocalDateTime to = criteria.to();

        validateDateRange(from, to);
        Pageable pageable = PageableUtils.build(
                criteria.page(), criteria.size(), criteria.sort(), "createdAt", SORT_FIELDS
        );

        Specification<SensorData> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (sensorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("sensor").get("id"), sensorId));
            }
            if (value != null) {
                predicates.add(criteriaBuilder.equal(root.get("value"), value));
            }
            if (rawTime != null && !rawTime.isBlank()) {
                DateTimeParts timeParts = DateTimeSearchUtils.parse(rawTime)
                        .orElseThrow(() -> new AppException(
                                HttpStatus.BAD_REQUEST,
                                "Time must use HH:mm, HH:mm:ss, DD/MM/YYYY or DD/MM/YYYY HH:mm[:ss]"
                        ));
                predicates.add(DateTimeSearchUtils.toPredicate(
                        criteriaBuilder, root.get("createdAt"), timeParts
                ));
            }
            if (rawKeyword != null && !rawKeyword.isBlank()) {
                String keyword = rawKeyword.trim();
                List<Predicate> keywordPredicates = new ArrayList<>();

                String sensorKeyword = resolveSensorKeyword(keyword);
                keywordPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sensor").get("name")),
                        "%" + sensorKeyword.toLowerCase(Locale.ROOT) + "%"
                ));

                parseNumericKeyword(keyword).ifPresent(numericValue -> keywordPredicates.add(
                        criteriaBuilder.equal(root.get("value"), numericValue)
                ));

                DateTimeSearchUtils.parse(keyword).ifPresent(parts -> keywordPredicates.add(
                        DateTimeSearchUtils.toPredicate(criteriaBuilder, root.get("createdAt"), parts)
                ));

                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return sensorDataRepository.findAll(specification, pageable);
    }

    private String resolveSensorKeyword(String rawKeyword) {
        String normalized = Normalizer.normalize(rawKeyword, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);

        if (normalized.contains("nhiet do") || normalized.contains("temperature")) {
            return "temperature";
        }
        if (normalized.contains("do am") || normalized.contains("humidity")) {
            return "humidity";
        }
        if (normalized.contains("anh sang") || normalized.contains("light")) {
            return "light";
        }
        return rawKeyword.trim();
    }

    private Optional<Double> parseNumericKeyword(String rawKeyword) {
        String normalized = rawKeyword.trim()
                .replace(',', '.')
                .replaceAll("(?i)\\s*(°?c|%|lux)$", "")
                .trim();
        try {
            return Optional.of(Double.valueOf(normalized));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public List<ChartDataResponse> getChartData() {
        List<SensorData> rows = new ArrayList<>(sensorDataRepository.findTop120ByOrderByCreatedAtDesc());
        Collections.reverse(rows);

        TreeMap<LocalDateTime, ChartDataResponse> grouped = new TreeMap<>();
        for (SensorData row : rows) {
            LocalDateTime time = row.getCreatedAt().truncatedTo(ChronoUnit.MINUTES);
            ChartDataResponse point = grouped.computeIfAbsent(
                    time,
                    key -> new ChartDataResponse(key, null, null, null)
            );

            String sensorName = row.getSensor().getName().trim().toLowerCase(Locale.ROOT);
            switch (sensorName) {
                case "temperature" -> point.setTemperature(row.getValue());
                case "humidity" -> point.setHumidity(row.getValue());
                case "light" -> point.setLight(row.getValue());
                default -> {
                }
            }
        }

        List<ChartDataResponse> result = new ArrayList<>(grouped.values());
        int start = Math.max(0, result.size() - 12);
        return result.subList(start, result.size());
    }

    @Transactional
    public SensorData create(CreateSensorDataRequest request) {
        Sensor sensor = sensorRepository.findById(request.getSensorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Sensor not found"));

        SensorData sensorData = new SensorData();
        sensorData.setSensor(sensor);
        sensorData.setValue(request.getValue());
        sensorData.setCreatedAt(request.getCreatedAt() == null ? LocalDateTime.now() : request.getCreatedAt());
        return sensorDataRepository.save(sensorData);
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "From time must be before to time");
        }
    }
}

