package com.smartlogi.sdms.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogi.sdms.application.dto.routes.LocationDTO;
import com.smartlogi.sdms.application.dto.routes.OptimizedRouteResponse;
import com.smartlogi.sdms.application.dto.routes.OptimizedRouteResponse.RouteDetails;
import com.smartlogi.sdms.application.dto.routes.RouteRequest;
import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.application.service.RouteOptimizationService;
import com.smartlogi.sdms.infrastructure.handler.GlobalExceptionHandler;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteOptimizationController.class)
@Import(GlobalExceptionHandler.class) // Assure que les handlers d'exception sont actifs
@WithMockUser // Simule un utilisateur authentifié
class RouteOptimizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RouteOptimizationService routeOptimizationService;

    // Mocks requis par Spring Security
    @MockBean
    private JWTService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private RouteRequest validRequest;
    private OptimizedRouteResponse nnResponse;
    private OptimizedRouteResponse cwResponse;

    @BeforeEach
    void setUp() {
        // --- 1. Préparation des données de base ---
        LocationDTO depot = new LocationDTO("DEPOT", 33.0, -7.0, BigDecimal.ZERO);
        LocationDTO locA = new LocationDTO("A", 33.1, -7.1, BigDecimal.TEN);
        RouteRequest.LivreurData livreur = new RouteRequest.LivreurData("L1", 100.0);

        validRequest = new RouteRequest(depot, List.of(locA), List.of(livreur));

        // --- 2. Préparation des réponses mockées (NearestNeighbor) ---
        RouteDetails nnDetails = RouteDetails.builder()
                .livreurId("L1")
                .stopOrder(List.of("A"))
                .routeDistanceKm(10.5)
                .routeEstimatedTimeHours(0.26)
                .routeLoad(10.0)
                .build();

        nnResponse = OptimizedRouteResponse.builder()
                .algorithm("NearestNeighbor")
                .tournees(List.of(nnDetails))
                .totalDistanceKm(10.5)
                .build();

        // --- 3. Préparation des réponses mockées (ClarkeWright) ---
        RouteDetails cwDetails = RouteDetails.builder()
                .livreurId("L1")
                .stopOrder(List.of("A"))
                .routeDistanceKm(11.0)
                .routeEstimatedTimeHours(0.275)
                .routeLoad(10.0)
                .build();

        cwResponse = OptimizedRouteResponse.builder()
                .algorithm("ClarkeWright")
                .tournees(List.of(cwDetails))
                .totalDistanceKm(11.0)
                .build();
    }

    // ------------------------------------------------------------------------
    // Tests pour POST /optimized
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("POST /optimized (NearestNeighbor) - Succès (200 OK)")
    void getOptimizedRoutes_NearestNeighbor_ShouldReturn200() throws Exception {
        // Arrange
        when(routeOptimizationService.optimizeRoutes(any(RouteRequest.class), eq("NearestNeighbor")))
                .thenReturn(nnResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/routes/optimized")
                        .with(csrf())
                        .param("algorithm", "NearestNeighbor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm", is("NearestNeighbor")))
                .andExpect(jsonPath("$.totalDistanceKm", is(10.5)));
    }

    @Test
    @DisplayName("POST /optimized - Algorithme non supporté (500 Internal Server Error)")
    void getOptimizedRoutes_UnsupportedAlgorithm_ShouldReturn500() throws Exception {
        // Arrange
        when(routeOptimizationService.optimizeRoutes(any(RouteRequest.class), eq("Dijkstra")))
                .thenThrow(new IllegalArgumentException("Algorithme non supporté: Dijkstra"));

        // Act & Assert
        // L'IllegalArgumentException est capturée par le handler générique de Exception.class (500)
        mockMvc.perform(post("/api/v1/routes/optimized")
                        .with(csrf())
                        .param("algorithm", "Dijkstra")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Une erreur interne est survenue. Veuillez contacter le support.")));
    }

    // ------------------------------------------------------------------------
    // Tests pour POST /compare
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("POST /compare - Succès (200 OK) avec les deux algorithmes")
    void compareAlgorithms_ShouldReturn200_WithBothResults() throws Exception {
        // Arrange
        // Mock l'appel pour NearestNeighbor
        when(routeOptimizationService.optimizeRoutes(any(RouteRequest.class), eq("NearestNeighbor")))
                .thenReturn(nnResponse);
        // Mock l'appel pour ClarkeWright
        when(routeOptimizationService.optimizeRoutes(any(RouteRequest.class), eq("ClarkeWright")))
                .thenReturn(cwResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/routes/compare")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                // Vérifier la présence et le contenu de NearestNeighbor
                .andExpect(jsonPath("$.NearestNeighbor.algorithm", is("NearestNeighbor")))
                .andExpect(jsonPath("$.NearestNeighbor.totalDistanceKm", is(10.5)))
                // Vérifier la présence et le contenu de ClarkeWright
                .andExpect(jsonPath("$.ClarkeWright.algorithm", is("ClarkeWright")))
                .andExpect(jsonPath("$.ClarkeWright.totalDistanceKm", is(11.0)));
    }
}