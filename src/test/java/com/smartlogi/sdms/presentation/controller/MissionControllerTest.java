package com.smartlogi.sdms.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogi.sdms.application.dto.mission.MissionRequestDTO;
import com.smartlogi.sdms.application.dto.mission.MissionResponseDTO;
import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.application.service.MissionService;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.enums.MissionType;
// --- CORRECTION : Import nécessaire ---
import com.smartlogi.sdms.domain.model.vo.Adresse;
import java.time.LocalDateTime;
// --- FIN CORRECTION ---
import com.smartlogi.sdms.infrastructure.handler.GlobalExceptionHandler;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MissionController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class MissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MissionService missionService;

    // Mocks requis par Spring Security
    @MockBean
    private JWTService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private MissionRequestDTO requestDTO;
    private MissionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // --- CORRECTION : Remplir tous les champs NotNull ---
        Adresse adresseValide = new Adresse("123", "Rue Test", "CASABLANCA", "20000", "MAROC", 33.5, -7.6);

        requestDTO = new MissionRequestDTO();
        requestDTO.setColisId("colis-123");
        requestDTO.setLivreurId("livreur-abc");
        requestDTO.setType(MissionType.COLLECTE);
        requestDTO.setDatePrevue(LocalDateTime.now().plusDays(1)); // @NotNull
        requestDTO.setOrigineAdresse(adresseValide); // @NotNull
        requestDTO.setDestinationAdresse(adresseValide); // @NotNull
        // --- FIN CORRECTION ---

        responseDTO = new MissionResponseDTO();
        responseDTO.setId("mission-xyz");
        responseDTO.setColisId("colis-123");
    }

    @Test
    @DisplayName("POST /create - Succès (201 CREATED)")
    void createMission_ShouldReturn201_WhenValid() throws Exception {
        // Arrange
        when(missionService.createMission(any(MissionRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/missions/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated()) // Attend 201
                .andExpect(jsonPath("$.id", is("mission-xyz")));
    }

    @Test
    @DisplayName("POST /create - Non trouvé (404 NOT FOUND)")
    void createMission_ShouldReturn404_WhenLivreurNotFound() throws Exception {
        // Arrange
        when(missionService.createMission(any(MissionRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Livreur introuvable"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/missions/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound()) // Attend 404
                .andExpect(jsonPath("$.message", is("Livreur introuvable")));
    }
}