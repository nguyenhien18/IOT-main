package com.iotroom.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserAccount {

    public enum Role {
        ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Transient
    private Role role = Role.ADMIN;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "student_code", nullable = false, length = 30)
    private String studentCode;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "figma_url", length = 255)
    private String figmaUrl;

    @Column(name = "postman_url", length = 255)
    private String postmanUrl;

    @Column(name = "report_url", length = 255)
    private String reportUrl;

    @Column(name = "avatar_url", columnDefinition = "LONGTEXT")
    private String avatarUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
