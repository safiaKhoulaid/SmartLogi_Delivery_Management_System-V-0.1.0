package com.smartlogi.sdms.presentation.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogi.sdms.application.dto.auth.AuthentificationRequest;
import com.smartlogi.sdms.application.dto.auth.AuthentificationResponse;
import com.smartlogi.sdms.application.dto.auth.RefreshTokenRequest; // Zedt hadi
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.application.service.AuthentificationService;
import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.application.service.RefreshTokenService;
import com.smartlogi.sdms.domain.model.entity.RefreshToken; // Zedt hadi
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import com.smartlogi.sdms.presentation.controller.AuthentificationController;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService; // Zedt hadi
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthentificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthentificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthentificationService authentificationService;

    // --- ZEDNA HADU L-MOCKS HIT CONTROLLER KAYHTAJHOM F REFRESH TOKEN ---
    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private BaseUserRepository userRepository;

    @MockBean
    private UserDetailsService userDetailsService; // Hada zedto hit context k-aytlbu

    @MockBean
    private Validator validator;

    private AuthentificationRequest authRequest;
    private UserRequestRegisterDTO registerRequest;
    private AuthentificationResponse authResponse;
    // Object jdid l test
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthentificationRequest();
        authRequest.setEmail("test@smartlogi.com");
        authRequest.setPassword("password123");

        registerRequest = new UserRequestRegisterDTO();
        registerRequest.setEmail("new@smartlogi.com");
        registerRequest.setPassword("password123");
        registerRequest.setNom("Test");
        registerRequest.setPrenom("User");
        registerRequest.setRole(Role.USER);

        authResponse = AuthentificationResponse.builder().token("dummy.jwt.token").build();

        // Setup Refresh Token request
        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setToken("valid-refresh-token");
    }

    @Test
    @DisplayName("POST /authenticate - Succès (200 OK)")
    void authenticate_ShouldReturn200_WhenValid() throws Exception {
        when(authentificationService.authenticate(any(AuthentificationRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(authResponse.getToken())));
    }

    @Test
    @DisplayName("POST /authenticate - Échec (401 UNAUTHORIZED)")
    void authenticate_ShouldReturn401_WhenBadCredentials() throws Exception {
        when(authentificationService.authenticate(any(AuthentificationRequest.class)))
                .thenThrow(new BadCredentialsException("Identifiants invalides"));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /register - Succès (200 OK)")
    void registerUser_ShouldReturn200_WhenValid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());
    }

    // --- HADA HOWA TEST JDID DYAL REFRESH TOKEN ---
    @Test
    @DisplayName("POST /refresh-token - Succès (200 OK)")
    void refreshToken_ShouldReturn200_WhenValid() throws Exception {
        // 1. Mock: Redis l9a token
        RefreshToken mockRedisToken = new RefreshToken("valid-refresh-token", "test@smartlogi.com");
        when(refreshTokenService.findByToken("valid-refresh-token")).thenReturn(mockRedisToken);

        // 2. Mock: User kayn f Database
        BaseUser mockUser = new BaseUser();
        mockUser.setEmail("test@smartlogi.com");
        when(userRepository.findByEmail("test@smartlogi.com")).thenReturn(Optional.of(mockUser));

        // 3. Mock: JWT Service generina token jdid
        when(jwtService.generateToken(any(BaseUser.class))).thenReturn("new.access.token");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("new.access.token"))) // Token jdid
                .andExpect(jsonPath("$.refreshToken", is("valid-refresh-token"))); // Token 9dim rje3
    }
}