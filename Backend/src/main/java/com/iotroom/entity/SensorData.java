package com.iotroom.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(
        name = "data_sensors",
        indexes = {
                @Index(
                        name = "idx_data_sensor_sensor_created_at",
                        columnList = "sensor_id, created_at"
                )
        }
)
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "sensor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_data_sensor_sensor")
    )
    private Sensor sensor;

    @Column(name = "`value`", nullable = false)
    private Double value;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
