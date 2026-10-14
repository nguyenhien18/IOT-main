package com.iotroom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.iotroom.entity.SensorData;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long>, JpaSpecificationExecutor<SensorData> {
    Optional<SensorData> findTopBySensor_IdOrderByCreatedAtDesc(Long sensorId);
    List<SensorData> findTop120ByOrderByCreatedAtDesc();
}

