package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.livreur.LivreurResponseDTO;
import com.smartlogi.sdms.application.mapper.LivreurMapper;
import com.smartlogi.sdms.application.service.LivreurService;
import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.infrastructure.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LivreurController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class LivreurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LivreurService livreurService;

    // Doit être mocké car il est injecté dans le contrôleur
    @MockBean
    private LivreurMapper livreurMapper;

    // Mocks requis par Spring Security
    @MockBean
    private JWTService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private String zoneId;
    private LivreurResponseDTO dto;

    @BeforeEach
    void setUp() {
        zoneId = "zone-123";
        dto = LivreurResponseDTO.builder().id("livreur-abc").nomComplet("Livreur Test").build();
    }

    @Test
    @DisplayName("GET /disponibles - Succès (200 OK)")
    void getAllDisponibleLivreur_ShouldReturn200_WhenFound() throws Exception {
        // Arrange
        ResponseEntity<List<LivreurResponseDTO>> response = ResponseEntity.ok(List.of(dto));
        when(livreurService.findAllDisponibleLivreurByZoneAssigneeId(zoneId))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/livreurs/disponibles")
                        .param("zoneId", zoneId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("livreur-abc")));
    }

    @Test
    @DisplayName("GET /disponibles - Non trouvé (404 NOT FOUND)")
    void getAllDisponibleLivreur_ShouldReturn404_WhenZoneNotFound() throws Exception {
        // Arrange
        when(livreurService.findAllDisponibleLivreurByZoneAssigneeId(zoneId))
                .thenThrow(new ResourceNotFoundException("La zone logistique est introuvable."));

        // Act & Assert
        mockMvc.perform(get("/api/v1/livreurs/disponibles")
                        .param("zoneId", zoneId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("La zone logistique est introuvable.")));
    }
}