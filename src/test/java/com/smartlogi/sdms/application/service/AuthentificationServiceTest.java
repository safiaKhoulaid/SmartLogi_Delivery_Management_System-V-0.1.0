package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.auth.AuthentificationRequest;
import com.smartlogi.sdms.application.dto.auth.AuthentificationResponse;
import com.smartlogi.sdms.application.dto.auth.RegisterResponse;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.domain.model.entity.RefreshToken;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyString;
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

    // --- NOUVEAU MOCK (Hit zedti Refresh Token logic) ---
    @Mock
    private RefreshTokenService refreshTokenService;

    // Le service à tester
    @InjectMocks
    private AuthentificationService authentificationService;

    // Données de test
    private UserRequestRegisterDTO registerDTO;
    private AuthentificationRequest authRequest;
    private BaseUser user;
    private RefreshToken dummyRefreshToken; // Objet l-fake dyal Redis
    private String dummyJwtToken = "dummy.jwt.token";

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

        // Initialiser Token d Redis
        dummyRefreshToken = new RefreshToken("refresh-token-uuid", "test@smartlogi.com");
    }

    // --- Tests pour register ---

    @Test
    @DisplayName("register devrait sauvegarder l'utilisateur et retourner les tokens")
    void register_ShouldSaveUserAndReturnToken() {
        String hashedPassword = "hashedPassword123";

        // 1. Mocks
        when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);
        when(baseUserRepository.save(any(BaseUser.class))).thenReturn(user);
        when(jwtService.generateToken(any(BaseUser.class))).thenReturn(dummyJwtToken);

        // [IMPORTANT] Ila knti kat-generi Refresh Token hta f Register, khassk t-zidi hadi:
        // when(refreshTokenService.createRefreshToken(anyString())).thenReturn(dummyRefreshToken);

        // Act
        RegisterResponse response = authentificationService.register(registerDTO);

        // Assert
        assertNotNull(response);
        // assertNotNull(response.getRefreshToken()); // Ila knti kat-rddih f RegisterResponse

        verify(baseUserRepository, times(1)).save(any(BaseUser.class));
        verify(jwtService, times(1)).generateToken(any(BaseUser.class));
    }

    // --- Tests pour authenticate ---

    @Test
    @DisplayName("authenticate devrait retourner JWT et Refresh Token si les identifiants sont valides")
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        // 1. User wajed
        when(baseUserRepository.findByEmail("test@smartlogi.com")).thenReturn(Optional.of(user));

        // 2. JWT Generation
        when(jwtService.generateToken(user)).thenReturn(dummyJwtToken);

        // 3. [IMPORTANT] Refresh Token Generation (Mockina l-partie jdida)
        when(refreshTokenService.createRefreshToken(user.getEmail())).thenReturn(dummyRefreshToken);

        // Act
        AuthentificationResponse response = authentificationService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals(dummyJwtToken, response.getToken());

        // [Vérification Jdida] Wach refresh token rje3?


        assertEquals("refresh-token-uuid", response.getRefreshToken());

        // Vérifier les appels
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenService, times(1)).createRefreshToken("test@smartlogi.com");
    }

    @Test
    @DisplayName("authenticate devrait lever une exception si l'AuthenticationManager échoue")
    void authenticate_ShouldThrowException_WhenManagerFails() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Mauvais identifiants"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authentificationService.authenticate(authRequest);
        });

        // S'assurer qu'on n'a jamais touché au Refresh Token Service
        verify(refreshTokenService, never()).createRefreshToken(anyString());
    }

    @Test
    @DisplayName("authenticate devrait lever NoSuchElementException si l'utilisateur n'est pas trouvé")
    void authenticate_ShouldThrowException_WhenUserNotFoundAfterAuth() {
        // Arrange
        when(baseUserRepository.findByEmail("test@smartlogi.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            authentificationService.authenticate(authRequest);
        });

        // S'assurer qu'on n'a jamais généré de token
        verify(refreshTokenService, never()).createRefreshToken(anyString());
    }
}