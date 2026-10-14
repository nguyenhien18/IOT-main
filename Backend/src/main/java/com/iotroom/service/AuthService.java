package com.iotroom.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iotroom.config.CustomUserDetailsService;
import com.iotroom.config.JwtService;
import com.iotroom.dto.request.LoginRequest;
import com.iotroom.dto.response.LoginResponse;
import com.iotroom.entity.UserAccount;
import com.iotroom.enums.ErrorCode;
import com.iotroom.exception.AppException;
import com.iotroom.repository.UserAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserAccount userAccount = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!passwordEncoder.matches(request.getPassword(), userAccount.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userAccount.getUsername());
        String token = jwtService.generateToken(userDetails);
        return new LoginResponse(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getRole().name(),
                token,
                "Bearer"
        );
    }
}

