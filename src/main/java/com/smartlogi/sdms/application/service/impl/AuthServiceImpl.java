package com.smartlogi.sdms.application.service.impl;

import com.smartlogi.sdms.application.service.AuthService;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.model.entity.users.Client;
import com.smartlogi.sdms.domain.repository.UserRepository;
import com.smartlogi.sdms.dto.AuthResponse;
import com.smartlogi.sdms.dto.RegisterRequest;
import com.smartlogi.sdms.infrastructure.security.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user (default to Client type)
        Client user = Client.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "USER")
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .build();

        BaseUser savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .message("User registered successfully")
                .build();
    }
}
