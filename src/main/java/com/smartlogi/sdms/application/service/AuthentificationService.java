package com.smartlogi.sdms.application.service;


import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import com.smartlogi.sdms.presentation.controller.auth.AuthentificationRequest;
import com.smartlogi.sdms.presentation.controller.auth.AuthentificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthentificationService {

    private final PasswordEncoder passwordEncoder;
    private final BaseUserRepository baseUserRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public AuthentificationResponse register(UserRequestRegisterDTO request) {
        BaseUser user =
                BaseUser.builder()
                        .firstName(request.getPrenom())
                        .lastName(request.getNom())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .adresse(request.getAdresse())
                        .telephone(request.getTelephone())
                        .role(request.getRole())
                        .build();

        // --- CORRECTION ---
        // 1. Récupérez l'entité sauvegardée (qui aura un ID)
        BaseUser savedUser = baseUserRepository.save(user);

        // 2. Passez l'entité sauvegardée au service JWT
        String jwt = jwtService.generateToken(savedUser);
        // --- FIN CORRECTION ---

        return AuthentificationResponse.builder()
                .token(jwt)
                .build();
    }

    public AuthentificationResponse authenticate(AuthentificationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        BaseUser user = baseUserRepository.findByEmail(request.getEmail())
                .orElseThrow();
        String jwt = jwtService.generateToken(user);
        return AuthentificationResponse.builder()
                .token(jwt)
                .build();

    }
}