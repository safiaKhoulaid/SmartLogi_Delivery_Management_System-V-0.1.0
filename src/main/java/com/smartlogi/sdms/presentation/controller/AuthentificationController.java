package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.auth.AuthentificationRequest;
import com.smartlogi.sdms.application.dto.auth.AuthentificationResponse;
import com.smartlogi.sdms.application.dto.auth.RegisterResponse;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.application.service.AuthentificationService;
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

}
