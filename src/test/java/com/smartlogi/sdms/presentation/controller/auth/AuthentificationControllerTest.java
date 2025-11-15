package com.smartlogi.sdms.presentation.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.application.service.AuthentificationService;
import com.smartlogi.sdms.domain.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthentificationController.class)
// --- CORRECTION : Désactivation de tous les filtres de sécurité pour ce test ---
@AutoConfigureMockMvc(addFilters = false)
class AuthentificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthentificationService authentificationService;

    // --- Mocks de sécurité non requis ---
    // @MockBean
    // private JWTService jwtService;
    // @MockBean
    // private UserDetailsService userDetailsService;

    private AuthentificationRequest authRequest;
    private UserRequestRegisterDTO registerRequest;
    private AuthentificationResponse authResponse;

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
        // Note: L'adresse et le téléphone peuvent être null,
        // @NotBlank n'est pas utilisé dans le DTO

        authResponse = new AuthentificationResponse("dummy.jwt.token");
    }

    @Test
    @DisplayName("POST /authenticate - Succès (200 OK)")
    void authenticate_ShouldReturn200_WhenValid() throws Exception {
        // Arrange
        when(authentificationService.authenticate(any(AuthentificationRequest.class)))
                .thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/authenticate")
                        // .with(csrf()) // <-- CORRECTION : Supprimé
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk()) // Attend 200
                .andExpect(jsonPath("$.token", is(authResponse.getToken())));
    }

    @Test
    @DisplayName("POST /authenticate - Échec (401 UNAUTHORIZED)")
    void authenticate_ShouldReturn401_WhenBadCredentials() throws Exception {
        // Arrange
        when(authentificationService.authenticate(any(AuthentificationRequest.class)))
                .thenThrow(new BadCredentialsException("Identifiants invalides"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/authenticate")
                        // .with(csrf()) // <-- CORRECTION : Supprimé
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized()); // Attend 401
    }

    @Test
    @DisplayName("POST /register - Succès (200 OK)")
    void registerUser_ShouldReturn200_WhenValid() throws Exception {
        // Arrange
        when(authentificationService.register(any(UserRequestRegisterDTO.class)))
                .thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        // .with(csrf()) // <-- CORRECTION : Supprimé
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk()) // Attend 200
                .andExpect(jsonPath("$.token", is(authResponse.getToken())));
    }
}