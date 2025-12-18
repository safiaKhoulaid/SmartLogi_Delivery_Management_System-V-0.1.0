package com.smartlogi.sdms.application.service;


import com.smartlogi.sdms.application.dto.auth.AuthentificationRequest;
import com.smartlogi.sdms.application.dto.auth.AuthentificationResponse;
import com.smartlogi.sdms.application.dto.auth.RegisterResponse;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.domain.exception.UserAlreadyExistsException;
import com.smartlogi.sdms.domain.model.entity.BlackListToken;
import com.smartlogi.sdms.domain.model.entity.RefreshToken;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import com.smartlogi.sdms.domain.repository.BlackListTokenRepository;
import com.smartlogi.sdms.domain.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthentificationService {

    private final PasswordEncoder passwordEncoder;
    private final BaseUserRepository baseUserRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final BlackListTokenRepository blackListTokenRepository ;
    private final RefreshTokenRepository refreshTokenRepository ;



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
                        .role(Role.USER)
                        .build();

        // 1. Récupérez l'entité sauvegardée (qui aura un ID)
        BaseUser savedUser = baseUserRepository.save(user);

        // 2. Passez l'entité sauvegardée au service JWT
        String jwt = jwtService.generateToken(savedUser);

        return RegisterResponse.builder()
                .message("Inscription réussie ! Veuillez vous connecter pour accéder à votre compte.")
                .email(savedUser.getEmail())
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
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        return AuthentificationResponse.builder()
                .token(jwt)
                .massage("Vous étés connecte par succès !")
                .refreshToken(refreshToken.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();

    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        jwt = authHeader.substring(7);

        // 1. Blacklist Access Token (باش نحرقو التوكن الحالي)
        BlackListToken blackListedToken =  BlackListToken.builder().accessToken(jwt).build();
        blackListTokenRepository.save(blackListedToken);

        // 2. Delete Refresh Token (باش نمنعوه يجدد التوكن)
        try {
            String userEmail = jwtService.extractUserEmail(jwt); // جبدنا الإيميل من التوكن

            // كنقلبو على Refresh Token ديال هاد السيد فـ Redis
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUsername(userEmail);

            if (refreshToken.isPresent()) {
                refreshTokenRepository.delete(refreshToken.get()); // مسحناه
                log.info("Refresh Token supprimé pour l'utilisateur: {}", userEmail);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du refresh token", e);
        }

        // 3. Clear Context
        SecurityContextHolder.clearContext();
    }
}