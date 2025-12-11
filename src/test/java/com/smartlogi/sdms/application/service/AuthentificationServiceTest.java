package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import com.smartlogi.sdms.application.dto.auth.AuthentificationRequest;
import com.smartlogi.sdms.application.dto.auth.AuthentificationResponse;
import com.smartlogi.sdms.application.dto.auth.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthentificationServiceTest {

    // Mocks pour les dépendances
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private BaseUserRepository baseUserRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JWTService jwtService;

    // Le service à tester
    @InjectMocks
    private AuthentificationService authentificationService;

    // Données de test
    private UserRequestRegisterDTO registerDTO;
    private AuthentificationRequest authRequest;
    private BaseUser user;
    private String dummyToken = "dummy.jwt.token";

    @BeforeEach
    void setUp() {
        Adresse adresse = new Adresse("1", "Rue Test", "TestVille", "10000", "MAROC", 33.0, -7.0);
        Telephone telephone = new Telephone("+212", "600000001");

        // DTO pour l'enregistrement
        registerDTO = new UserRequestRegisterDTO();
        registerDTO.setNom("TestUserNom");
        registerDTO.setPrenom("TestUserPrenom");
        registerDTO.setEmail("test@smartlogi.com");
        registerDTO.setPassword("password123");
        registerDTO.setRole(Role.USER);
        registerDTO.setAdresse(adresse);
        registerDTO.setTelephone(telephone);

        // Requête d'authentification
        authRequest = new AuthentificationRequest();
        authRequest.setEmail("test@smartlogi.com");
        authRequest.setPassword("password123");

        // Entité utilisateur
        user = BaseUser.builder()
                .id("user-123")
                .firstName("TestUserPrenom")
                .lastName("TestUserNom")
                .email("test@smartlogi.com")
                .password("hashedPassword")
                .role(Role.USER)
                .build();
    }

    // --- Tests pour register ---

    @Test
    @DisplayName("register devrait sauvegarder l'utilisateur et retourner un token")
    void register_ShouldSaveUserAndReturnToken() {
        //

        String hashedPassword = "hashedPassword123";

        // 1. Simuler l'encodage du mot de passe
        when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);

        // 2. Simuler la sauvegarde (on capture l'argument pour vérification)
        ArgumentCaptor<BaseUser> userCaptor = ArgumentCaptor.forClass(BaseUser.class);
        when(baseUserRepository.save(userCaptor.capture())).thenReturn(user);

        // 3. Simuler la génération du token
        when(jwtService.generateToken(any(BaseUser.class))).thenReturn(dummyToken);

        // Act
        RegisterResponse response = authentificationService.register(registerDTO);

        // Assert
        assertNotNull(response);

        // Vérifier l'utilisateur capturé avant la sauvegarde
        BaseUser capturedUser = userCaptor.getValue();
        assertEquals("TestUserPrenom", capturedUser.getFirstName());
        assertEquals("TestUserNom", capturedUser.getLastName());
        assertEquals("test@smartlogi.com", capturedUser.getEmail());
        assertEquals(hashedPassword, capturedUser.getPassword()); // Vérifie que le mdp est encodé
        assertEquals(Role.USER, capturedUser.getRole());

        // Vérifier les appels
        verify(passwordEncoder, times(1)).encode("password123");
        verify(baseUserRepository, times(1)).save(any(BaseUser.class));
        verify(jwtService, times(1)).generateToken(user);
    }

    // --- Tests pour authenticate ---

    @Test
    @DisplayName("authenticate devrait retourner un token si les identifiants sont valides")
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        // 1. Simuler l'AuthenticationManager (ne lève pas d'exception)
        // (Rien à faire pour 'when' car la méthode est void, on vérifie juste l'appel)

        // 2. Simuler la récupération de l'utilisateur
        when(baseUserRepository.findByEmail("test@smartlogi.com")).thenReturn(Optional.of(user));

        // 3. Simuler la génération du token
        when(jwtService.generateToken(user)).thenReturn(dummyToken);

        // Act
        AuthentificationResponse response = authentificationService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals(dummyToken, response.getToken());

        // Vérifier les appels
        verify(authenticationManager, times(1)).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
        verify(baseUserRepository, times(1)).findByEmail("test@smartlogi.com");
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    @DisplayName("authenticate devrait lever une exception si l'AuthenticationManager échoue")
    void authenticate_ShouldThrowException_WhenManagerFails() {
        // Arrange
        // 1. Simuler l'échec de l'AuthenticationManager (ex: mauvais mot de passe)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Mauvais identifiants"));

        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authentificationService.authenticate(authRequest);
        });

        assertEquals("Mauvais identifiants", exception.getMessage());

        // S'assurer qu'on n'a jamais cherché l'utilisateur ou généré de token
        verify(baseUserRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("authenticate devrait lever NoSuchElementException si l'utilisateur n'est pas trouvé (cas anormal)")
    void authenticate_ShouldThrowException_WhenUserNotFoundAfterAuth() {
        // Arrange
        // 1. Simuler l'AuthenticationManager (succès)
        // (Rien à faire)

        // 2. Simuler la récupération de l'utilisateur (échec, ne devrait pas arriver si l'auth réussit)
        when(baseUserRepository.findByEmail("test@smartlogi.com")).thenReturn(Optional.empty());

        // Act & Assert
        // Le orElseThrow() dans le service lève cette exception
        assertThrows(NoSuchElementException.class, () -> {
            authentificationService.authenticate(authRequest);
        });

        // Vérifier les appels
        verify(authenticationManager, times(1)).authenticate(any());
        verify(baseUserRepository, times(1)).findByEmail("test@smartlogi.com");

        // S'assurer qu'on n'a jamais généré de token
        verify(jwtService, never()).generateToken(any());
    }

}