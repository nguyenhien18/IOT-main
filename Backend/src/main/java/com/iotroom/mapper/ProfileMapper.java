package com.iotroom.mapper;

import org.springframework.stereotype.Component;

import com.iotroom.dto.response.ProfileResponse;
import com.iotroom.entity.UserAccount;

@Component
public class ProfileMapper {
    public ProfileResponse toProfileResponse(UserAccount entity) {
        return new ProfileResponse(
                entity.getFullName(),
                entity.getStudentCode(),
                entity.getClassName(),
                entity.getEmail(),
                entity.getGithubUrl(),
                entity.getFigmaUrl(),
                entity.getPostmanUrl(),
                entity.getReportUrl(),
                entity.getAvatarUrl()
        );
    }
}

