package com.iotroom.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Full name must not be blank")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Student code must not be blank")
    @Size(max = 30, message = "Student code must not exceed 30 characters")
    private String studentCode;

    @NotBlank(message = "Class name must not be blank")
    @Size(max = 50, message = "Class name must not exceed 50 characters")
    private String className;

    @Email(message = "Email is invalid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 255, message = "GitHub URL must not exceed 255 characters")
    private String githubUrl;

    @Size(max = 255, message = "Figma URL must not exceed 255 characters")
    private String figmaUrl;

    @Size(max = 255, message = "Postman URL must not exceed 255 characters")
    private String postmanUrl;

    @Size(max = 255, message = "Report URL must not exceed 255 characters")
    private String reportUrl;

    private String avatarUrl;
}
