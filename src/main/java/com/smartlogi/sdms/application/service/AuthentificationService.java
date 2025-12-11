package com.smartlogi.sdms.application.service;


import com.smartlogi.sdms.application.dto.auth.AuthentificationRequest;
import com.smartlogi.sdms.application.dto.auth.AuthentificationResponse;
import com.smartlogi.sdms.application.dto.auth.RegisterResponse;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.domain.exception.UserAlreadyExistsException;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
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

    public RegisterResponse register(UserRequestRegisterDTO request) {
        String email = request.getEmail();
        if (baseUserRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("ce email déja utilise");
        }

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

        return RegisterResponse.builder()
                .message("Inscription réussie ! Veuillez vous connecter pour accéder à votre compte.")
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
                .massage("Vous étés connecte par succès !")
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

    }
}