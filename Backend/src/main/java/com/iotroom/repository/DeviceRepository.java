package com.iotroom.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iotroom.entity.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByNameIgnoreCase(String name);
}

