package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.auth.*;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.application.service.AuthentificationService;
import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.application.service.RefreshTokenService;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.RefreshToken;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor

public class AuthentificationController {

    private final AuthentificationService authentificationService;
    private final RefreshTokenService refreshTokenService;
    private final BaseUserRepository baseUserRepository;
    private final JWTService jwtService;

    //=========ENDPOINT DE LOGIN==========================
    @PostMapping("/authenticate")
    public ResponseEntity<AuthentificationResponse> authenticate(@Valid
                                                                 @RequestBody AuthentificationRequest request) {
        return ResponseEntity.ok(authentificationService.authenticate(request));
    }


    //=========ENDPOINT DE REGISTER==========================
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid
                                                         @RequestBody UserRequestRegisterDTO request) {
        return ResponseEntity.ok(authentificationService.register(request));
    }


    //=========ENDPOINT DE REFRESH_TOKEN ==========================
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {

        String requestToken = request.getToken();

        RefreshToken token = refreshTokenService.findByToken(requestToken);

        var user = baseUserRepository.findByEmail(token.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        String newAccessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(new JwtResponse(newAccessToken, requestToken));
    }



}
