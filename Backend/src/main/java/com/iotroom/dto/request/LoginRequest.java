package com.iotroom.dto.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username must not be blank")
    @Schema(example = "admin")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Schema(example = "123456", format = "password")
    private String password;
}
