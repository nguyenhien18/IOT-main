package com.iotroom.dto.response;

public record ProfileResponse(
        String fullName,
        String studentCode,
        String className,
        String email,
        String githubUrl,
        String figmaUrl,
        String postmanUrl,
        String reportUrl,
        String avatarUrl
) {
}

