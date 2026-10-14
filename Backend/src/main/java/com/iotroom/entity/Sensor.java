package com.iotroom.entity;

import java.util.Locale;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public String getUnit() {
        return switch (name == null ? "" : name.trim().toUpperCase(Locale.ROOT)) {
            case "TEMPERATURE" -> "°C";
            case "HUMIDITY" -> "%";
            case "LIGHT" -> "lux";
            default -> "";
        };
    }
}
