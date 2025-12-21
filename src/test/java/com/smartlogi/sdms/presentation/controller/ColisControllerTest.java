package com.smartlogi.sdms.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.application.service.ColisService;
import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import com.smartlogi.sdms.infrastructure.handler.GlobalExceptionHandler;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
// AJOUT DE L'IMPORT STATIQUE POUR CSRF
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour ColisController.
 * Utilise @WebMvcTest pour tester la couche contrôleur uniquement, en moquant les services.
 * Importe GlobalExceptionHandler pour tester les réponses d'erreur HTTP correctes.
 */
@WebMvcTest(ColisController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser // Simule un utilisateur authentifié pour tous les tests
class ColisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ColisService colisService;

    @MockBean
    private ColisMapper colisMapper;

    // --- Mocks requis par Spring Security (même s'ils ne sont pas utilisés) ---
    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    // --- Données de test réutilisables ---
    private ColisRequestDTO requestDTO;
    private ColisResponseDTO responseDTO;
    private Colis colisEntity;
    private String clientId;
    private String colisId;

    @BeforeEach
    void setUp() {
        clientId = "client-123";
        colisId = "colis-abc";

        requestDTO = new ColisRequestDTO();
        requestDTO.setExpediteurId(clientId);
        requestDTO.setDescription("Test DTO");
        // ... (initialiser d'autres champs si nécessaire)

        colisEntity = new Colis();
        colisEntity.setId(colisId);
        colisEntity.setDescription("Test Entity");
        colisEntity.setStatut(StatusColis.CREE);
        colisEntity.setPriorite(PriorityColis.NORMALE);

        responseDTO = ColisResponseDTO.builder()
                .id(colisId)
                .description("Test DTO")
                .statut(StatusColis.CREE)
                .dateCreation(LocalDateTime.now())
                .build();
    }

    // --- Tests pour createColis ---

    @Test
    @DisplayName("POST /api/v1/colis/create - Succès (201 CREATED)")
    void createColis_ShouldReturn201_WhenValid() throws Exception {
        // Arrange
        when(colisService.createColis(any(ColisRequestDTO.class))).thenReturn(responseDTO);
        when(colisMapper.toColisResponseDTO(any(Colis.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/colis/create")
                        .with(csrf()) // <-- AJOUT DU JETON CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated()) // Vérifie le statut 201
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(colisId)))
                .andExpect(jsonPath("$.description", is(responseDTO.getDescription())));

        // Vérifie que les logs du contrôleur (log.info) ont été appelés
        verify(colisService, times(1)).createColis(any(ColisRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/colis/create - Erreur de validation (400 BAD REQUEST)")
    void createColis_ShouldReturn400_WhenServiceThrowsValidationException() throws Exception {
        // Arrange
        when(colisService.createColis(any(ColisRequestDTO.class)))
                .thenThrow(new ValidationException("Erreur de validation métier"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/colis/create")
                        .with(csrf()) // <-- AJOUT DU JETON CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest()) // Géré par GlobalExceptionHandler
                .andExpect(jsonPath("$.message", is("Erreur de validation métier")));
    }

    // --- Tests pour getAllColisByClientExpediteurId ---

    @Test
    @DisplayName("GET /api/v1/colis/client/{idClient} - Succès (200 OK)")
    void getAllColisByClientExpediteurId_ShouldReturn200_WhenFound() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<ColisResponseDTO> page = new PageImpl<>(List.of(responseDTO), pageable, 1);

        when(colisService.getColisByClientExpediteurId(eq(clientId), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/colis/client/{idClient}", clientId) // PAS BESOIN DE CSRF POUR GET
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].id", is(colisId)));
    }

    @Test
    @DisplayName("GET /api/v1/colis/client/{idClient} - Non trouvé (404 NOT FOUND)")
    void getAllColisByClientExpediteurId_ShouldReturn404_WhenServiceThrowsResourceNotFound() throws Exception {
        // Arrange
        when(colisService.getColisByClientExpediteurId(eq(clientId), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Client non trouvé"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/colis/client/{idClient}", clientId) // PAS BESOIN DE CSRF POUR GET
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Géré par GlobalExceptionHandler
                .andExpect(jsonPath("$.message", is("Client non trouvé")));
    }

    // --- Tests pour updateColis ---

    @Test
    @DisplayName("PUT /api/v1/colis/{id} - Succès (200 OK)")
    void updateColis_ShouldReturn200_WhenValid() throws Exception {
        // Arrange
        ColisResponseDTO updatedResponse = responseDTO;
        updatedResponse.setDescription("Description mise à jour");

        when(colisService.updateColis(eq(colisId), any(ColisRequestDTO.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/colis/{id}", colisId)
                        .with(csrf()) // <-- AJOUT DU JETON CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(colisId)))
                .andExpect(jsonPath("$.description", is("Description mise à jour")));
    }

    @Test
    @DisplayName("PUT /api/v1/colis/{id} - Non trouvé (404 NOT FOUND)")
    void updateColis_ShouldReturn404_WhenServiceThrowsResourceNotFound() throws Exception {
        // Arrange
        when(colisService.updateColis(eq(colisId), any(ColisRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Colis non trouvé"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/colis/{id}", colisId)
                        .with(csrf()) // <-- AJOUT DU JETON CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound()) // Géré par GlobalExceptionHandler
                .andExpect(jsonPath("$.message", is("Colis non trouvé")));
    }

    // --- Tests pour deleteColis ---

    @Test
    @DisplayName("DELETE /api/v1/colis/{id} - Succès (204 NO CONTENT)")
    void deleteColis_ShouldReturn204_WhenValid() throws Exception {
        // Arrange
        doNothing().when(colisService).deleteColis(eq(colisId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/colis/{id}", colisId)
                        .with(csrf())) // <-- AJOUT DU JETON CSRF
                .andExpect(status().isNoContent()); // Vérifie le statut 204

        verify(colisService, times(1)).deleteColis(colisId);
    }

    @Test
    @DisplayName("DELETE /api/v1/colis/{id} - Non trouvé (404 NOT FOUND)")
    void deleteColis_ShouldReturn404_WhenServiceThrowsResourceNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Colis non trouvé pour suppression"))
                .when(colisService).deleteColis(eq(colisId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/colis/{id}", colisId)
                        .with(csrf())) // <-- AJOUT DU JETON CSRF
                .andExpect(status().isNotFound()) // Géré par GlobalExceptionHandler
                .andExpect(jsonPath("$.message", is("Colis non trouvé pour suppression")));
    }

    // --- Test de couverture pour les exceptions génériques ---

    @Test
    @DisplayName("GET /api/v1/colis/client/{idClient} - Erreur interne (500 INTERNAL SERVER ERROR)")
    void anyEndpoint_ShouldReturn500_WhenServiceThrowsGenericException() throws Exception {
        // Arrange
        when(colisService.getColisByClientExpediteurId(eq(clientId), any(Pageable.class)))
                .thenThrow(new RuntimeException("Erreur base de données inattendue"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/colis/client/{idClient}", clientId) // PAS BESOIN DE CSRF POUR GET
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // Géré par GlobalExceptionHandler
                .andExpect(jsonPath("$.message", is("Une erreur interne est survenue. Veuillez contacter le support.")));
    }
}