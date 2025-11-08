package com.smartlogi.sdms.presentation.controller.auth;

import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.application.service.AuthentificationService;
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

    @PostMapping("/authenticate")
    public ResponseEntity<AuthentificationResponse> authenticate(
            @RequestBody AuthentificationRequest request) {
        return ResponseEntity.ok(authentificationService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthentificationResponse> registerUser(
            @RequestBody UserRequestRegisterDTO request) {
        return ResponseEntity.ok(authentificationService.register(request));
    }

}
