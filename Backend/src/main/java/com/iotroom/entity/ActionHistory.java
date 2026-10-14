package com.iotroom.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "actions",
        indexes = {
                @Index(name = "idx_action_user_created_at", columnList = "user_id, created_at"),
                @Index(name = "idx_action_device_created_at", columnList = "device_id, created_at"),
                @Index(name = "idx_action_status", columnList = "status")
        }
)
public class ActionHistory {

    public enum Action {
        ON,
        OFF
    }

    public enum Status {
        PROCESSING,
        SUCCESS,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "device_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_action_device")
    )
    private Device device;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_action_user")
    )
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private Action action;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
