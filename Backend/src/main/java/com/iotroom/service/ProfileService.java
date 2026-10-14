package com.iotroom.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iotroom.dto.request.UpdateProfileRequest;
import com.iotroom.entity.UserAccount;
import com.iotroom.exception.AppException;
import com.iotroom.repository.UserAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public UserAccount getCurrentProfile() {
        return getCurrentUser();
    }

    @Transactional
    public UserAccount updateCurrentProfile(UpdateProfileRequest request) {
        UserAccount userAccount = getCurrentUser();
        userAccount.setFullName(request.getFullName());
        userAccount.setStudentCode(request.getStudentCode());
        userAccount.setClassName(request.getClassName());
        userAccount.setEmail(request.getEmail());
        userAccount.setGithubUrl(request.getGithubUrl());
        userAccount.setFigmaUrl(request.getFigmaUrl());
        userAccount.setPostmanUrl(request.getPostmanUrl());
        userAccount.setReportUrl(request.getReportUrl());
        userAccount.setAvatarUrl(request.getAvatarUrl());
        return userAccountRepository.save(userAccount);
    }

    private UserAccount getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        return userAccountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
    }
}

