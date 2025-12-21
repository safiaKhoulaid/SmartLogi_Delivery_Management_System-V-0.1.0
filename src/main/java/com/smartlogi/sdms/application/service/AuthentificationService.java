package com.smartlogi.sdms.application.service;


import com.smartlogi.sdms.application.dto.auth.AuthentificationRequest;
import com.smartlogi.sdms.application.dto.auth.AuthentificationResponse;
import com.smartlogi.sdms.application.dto.auth.RegisterResponse;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.domain.exception.UserAlreadyExistsException;
import com.smartlogi.sdms.domain.model.entity.RefreshToken;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import com.smartlogi.sdms.domain.repository.ClientExpediteurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthentificationService {

    private final PasswordEncoder passwordEncoder;
    private final BaseUserRepository baseUserRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ClientExpediteurRepository clientExpediteurRepository;


    @Transactional
    public RegisterResponse register(UserRequestRegisterDTO request) {

        if (baseUserRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Cet email est déjà utilisé.");
        }


        ClientExpediteur newClient = ClientExpediteur.builder()
                .firstName(request.getPrenom())
                .lastName(request.getNom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .codeClient("CL-" + System.currentTimeMillis())
                .role(Role.USER)
                .build();


        ClientExpediteur savedClient = clientExpediteurRepository.save(newClient);


        return RegisterResponse.builder()
                .message("Inscription réussie en tant que Client !")
                .email(savedClient.getEmail())
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


}