package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.auth.*;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.application.service.AuthentificationService;
import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.application.service.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService ;
    private final BaseUserRepository baseUserRepository ;
    private final JWTService jwtService ;

    //=========ENDPOINT DE LOGIN==========================
    @PostMapping("/authenticate")
    public ResponseEntity<AuthentificationResponse> authenticate(@Valid
                                                                 @RequestBody AuthentificationRequest request) {
        return ResponseEntity.ok(authentificationService.authenticate(request));
    }


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid
                                                         @RequestBody UserRequestRegisterDTO request) {
        return ResponseEntity.ok(authentificationService.register(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {

        String requestToken = request.getToken();

        // 1. Jib Token mn Redis (Hadi gha t-lo7 Exception ila malqatouch, donc ma-khtajinch .map)
        RefreshToken token = refreshTokenService.findByToken(requestToken);

        // 2. Jib User mn MySQL
        var user = baseUserRepository.findByEmail(token.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        // 3. Generi Access Token Jdid
        String newAccessToken = jwtService.generateToken(user);

        // 4. Rddi Jawab
        return ResponseEntity.ok(new JwtResponse(newAccessToken, requestToken));
    }

}
