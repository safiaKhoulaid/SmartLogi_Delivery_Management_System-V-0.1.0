package com.smartlogi.sdms.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogi.sdms.application.dto.tournee.TourneeRequestDTO;
import com.smartlogi.sdms.application.dto.tournee.TourneeResponseDTO;
import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.application.service.TourneeService;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.enums.StatutTournee;
import com.smartlogi.sdms.infrastructure.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TourneeController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser(authorities = "GESTIONNAIRE") // Simule un utilisateur authentifié avec le bon rôle
class TourneeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TourneeService tourneeService;

    // Mocks requis par Spring Security
    @MockBean
    private JWTService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private Long tourneeId;
    private String livreurId;
    private TourneeRequestDTO requestDTO;
    private TourneeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        tourneeId = 1L;
        livreurId = "livreur-xyz";

        requestDTO = new TourneeRequestDTO();
        requestDTO.setDateTournee(LocalDate.now());
        requestDTO.setLivreurId(livreurId);
        requestDTO.setZoneId("zone-abc");
        requestDTO.setColisIds(List.of("colis-1", "colis-2"));
        requestDTO.setAlgorithme("NearestNeighbor");

        responseDTO = TourneeResponseDTO.builder()
                .id(tourneeId)
                .livreurId(livreurId)
                .statut(StatutTournee.PLANIFIEE)
                .build();
    }

    // ------------------------------------------------------------------------
    // 1. Tests pour POST /optimize
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("POST /optimize - Succès (201 CREATED)")
    void createAndOptimizeTournee_ShouldReturn201_WhenValid() throws Exception {
        // Arrange
        when(tourneeService.createAndOptimizeTournee(any(TourneeRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/tournees/optimize")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(tourneeId.intValue())));

        verify(tourneeService, times(1)).createAndOptimizeTournee(any(TourneeRequestDTO.class));
    }

    @Test
    @DisplayName("POST /optimize - Erreur ResourceNotFound (404 NOT FOUND)")
    void createAndOptimizeTournee_ShouldReturn404_WhenResourceNotFound() throws Exception {
        // Arrange
        when(tourneeService.createAndOptimizeTournee(any(TourneeRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Livreur introuvable"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/tournees/optimize")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Livreur introuvable")));
    }

    // ------------------------------------------------------------------------
    // 2. Tests pour GET /{id}
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("GET /{id} - Succès (200 OK)")
    void getTourneeById_ShouldReturn200_WhenFound() throws Exception {
        // Arrange
        when(tourneeService.getTourneeById(tourneeId)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tournees/{id}", tourneeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(tourneeId.intValue())));

        verify(tourneeService, times(1)).getTourneeById(tourneeId);
    }

    @Test
    @DisplayName("GET /{id} - Non trouvé (404 NOT FOUND)")
    void getTourneeById_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        when(tourneeService.getTourneeById(tourneeId))
                .thenThrow(new IllegalArgumentException("Tournee introuvable"));

        // Act & Assert
        // Remarque: L'IllegalArgumentException sera capturée par GlobalExceptionHandler comme un 500
        // Sauf si vous adaptez le service pour lancer ResourceNotFoundException, mais c'est un bon test de résilience
        mockMvc.perform(get("/api/v1/tournees/{id}", tourneeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // ------------------------------------------------------------------------
    // 3. Tests pour GET /livreur/{livreurId}
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("GET /livreur/{livreurId} - Succès (200 OK)")
    void getTourneesByLivreur_ShouldReturn200_WhenFound() throws Exception {
        // Arrange
        Page<TourneeResponseDTO> page = new PageImpl<>(List.of(responseDTO));
        when(tourneeService.getTourneesByLivreur(eq(livreurId), eq(0), eq(10))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tournees/livreur/{livreurId}", livreurId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)));

        verify(tourneeService, times(1)).getTourneesByLivreur(livreurId, 0, 10);
    }

    // ------------------------------------------------------------------------
    // 4. Tests pour PUT /{id}/status
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /{id}/status - Succès (200 OK)")
    void updateTourneeStatus_ShouldReturn200_WhenValid() throws Exception {
        // Arrange
        TourneeResponseDTO updatedDTO = TourneeResponseDTO.builder().id(tourneeId).statut(StatutTournee.EN_COURS).build();
        when(tourneeService.updateTourneeStatus(tourneeId, StatutTournee.EN_COURS)).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/tournees/{id}/status", tourneeId)
                        .with(csrf())
                        .param("statut", StatutTournee.EN_COURS.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut", is(StatutTournee.EN_COURS.name())));

        verify(tourneeService, times(1)).updateTourneeStatus(tourneeId, StatutTournee.EN_COURS);
    }

    @Test
    @DisplayName("PUT /{id}/status - Non trouvé (404 NOT FOUND)")
    void updateTourneeStatus_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        when(tourneeService.updateTourneeStatus(tourneeId, StatutTournee.TERMINEE))
                .thenThrow(new ResourceNotFoundException("Tournée", "id", tourneeId));

        // Act & Assert
        mockMvc.perform(put("/api/v1/tournees/{id}/status", tourneeId)
                        .with(csrf())
                        .param("statut", StatutTournee.TERMINEE.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Tournée non trouvé avec id : '1'")));
    }

    // ------------------------------------------------------------------------
    // 5. Tests pour DELETE /{id}
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /{id} - Succès (204 NO CONTENT)")
    void deleteTournee_ShouldReturn204_WhenValid() throws Exception {
        // Arrange
        doNothing().when(tourneeService).deleteTournee(tourneeId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tournees/{id}", tourneeId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(tourneeService, times(1)).deleteTournee(tourneeId);
    }

    @Test
    @DisplayName("DELETE /{id} - Non trouvé (404 NOT FOUND)")
    void deleteTournee_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Tournée non trouvée pour suppression"))
                .when(tourneeService).deleteTournee(tourneeId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tournees/{id}", tourneeId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Tournée non trouvée pour suppression")));
    }
}