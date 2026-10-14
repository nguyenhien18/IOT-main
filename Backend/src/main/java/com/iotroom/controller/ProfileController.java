package com.iotroom.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iotroom.dto.request.UpdateProfileRequest;
import com.iotroom.dto.response.ApiResponse;
import com.iotroom.dto.response.ProfileResponse;
import com.iotroom.mapper.ProfileMapper;
import com.iotroom.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    @GetMapping
    public ApiResponse<ProfileResponse> getProfile() {
        return ApiResponse.ok(profileMapper.toProfileResponse(profileService.getCurrentProfile()));
    }

    @PutMapping
    public ApiResponse<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(profileMapper.toProfileResponse(profileService.updateCurrentProfile(request)));
    }
}

