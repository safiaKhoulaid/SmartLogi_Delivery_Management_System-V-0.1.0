package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.routes.LocationDTO;
import com.smartlogi.sdms.application.dto.routes.OptimizedRouteResponse;
import com.smartlogi.sdms.application.dto.routes.RouteRequest;
import com.smartlogi.sdms.infrastructure.utils.Haversine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
// AJOUT DE L'IMPORT POUR LA RÉFLEXION
import java.lang.reflect.Method;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

// Pas besoin de Mockito ici, car c'est un test de logique pure
class RouteOptimizationServiceTest {

    // Le service à tester
    private RouteOptimizationService routeOptimizationService;

    // Données de test
    private LocationDTO depot;
    private LocationDTO locA;
    private LocationDTO locB;
    private RouteRequest.LivreurData livreur;

    @BeforeEach
    void setUp() {
        // Instanciation directe du service
        routeOptimizationService = new RouteOptimizationService();

        // Le dépôt est à l'origine (0, 0)
        depot = new LocationDTO("DEPOT", 0.0, 0.0, BigDecimal.ZERO);

        // Location A (proche)
        locA = new LocationDTO("A", 1.0, 1.0, BigDecimal.valueOf(10));

        // Location B (plus loin)
        locB = new LocationDTO("B", 5.0, 5.0, BigDecimal.valueOf(20));

        // Un livreur avec une capacité suffisante
        livreur = new RouteRequest.LivreurData("L1", 100.0);
    }

    // --- NOUVEAU TEST POUR calculateDistanceMatrix (méthode privée) ---
    @Test
    @DisplayName("calculateDistanceMatrix devrait créer une matrice (N+1)x(N+1) et calculer les distances Haversine")
    void calculateDistanceMatrix_ShouldCreateCorrectSizedMatrixAndSymmetricDistances() throws Exception {
        // Arrange
        // Coordonnées réelles pour avoir des distances non nulles (Casa et Rabat)
        LocationDTO depotCasa = new LocationDTO("DEPOT", 33.5731, -7.5898, BigDecimal.ZERO);
        LocationDTO locRabat = new LocationDTO("RABAT", 34.0209, -6.8417, BigDecimal.valueOf(10));
        LocationDTO locCasaSame = new LocationDTO("CASA2", 33.5731, -7.5898, BigDecimal.valueOf(5)); // Même point que dépôt
        List<LocationDTO> locations = List.of(locRabat, locCasaSame); // N=2 locations

        // La taille attendue est (2+1) x (2+1) = 3x3
        int expectedSize = locations.size() + 1;

        // 1. Récupérer la méthode privée via Reflection
        Method method = RouteOptimizationService.class.getDeclaredMethod(
                "calculateDistanceMatrix",
                LocationDTO.class,
                List.class
        );
        method.setAccessible(true); // Rendre la méthode accessible

        // Act
        // 2. Invoquer la méthode privée
        double[][] matrix = (double[][]) method.invoke(routeOptimizationService, depotCasa, locations);

        // Assert
        // 1. Taille de la matrice
        assertNotNull(matrix);
        assertEquals(expectedSize, matrix.length); // 3 lignes
        assertEquals(expectedSize, matrix[0].length); // 3 colonnes

        // Les points sont indexés comme : 0=Depot(Casa), 1=LocRabat, 2=LocCasaSame

        // 2. Distance D-D (0-0) doit être 0
        assertEquals(0.0, matrix[0][0], 0.001);

        // 3. Distance d'un point à lui-même (2-2) doit être 0
        assertEquals(0.0, matrix[2][2], 0.001);

        // 4. Distances calculées :
        // D-Rabat (0-1) doit être non nul
        double distD_Rabat = Haversine.distance(depotCasa.getLatitude(), depotCasa.getLongitude(), locRabat.getLatitude(), locRabat.getLongitude());
        assertEquals(distD_Rabat, matrix[0][1], 0.001);
        // D-CasaSame (0-2) doit être 0 (mêmes coordonnées)
        assertEquals(0.0, matrix[0][2], 0.001);

        // 5. Vérification de la symétrie : dist(i, j) = dist(j, i)
        // Rabat-D (1-0) = D-Rabat (0-1)
        assertEquals(matrix[0][1], matrix[1][0], 0.001);
        // CasaSame-D (2-0) = D-CasaSame (0-2)
        assertEquals(matrix[0][2], matrix[2][0], 0.001);
        // Rabat-CasaSame (1-2)
        double distRabat_CasaSame = Haversine.distance(locRabat.getLatitude(), locRabat.getLongitude(), locCasaSame.getLatitude(), locCasaSame.getLongitude());
        assertEquals(distRabat_CasaSame, matrix[1][2], 0.001);
        assertEquals(matrix[1][2], matrix[2][1], 0.001);
    }
    // --- FIN NOUVEAU TEST ---

    @Test
    @DisplayName("NearestNeighbor devrait retourner la tournée dans l'ordre A, B")
    void optimizeRoutes_NearestNeighbor_ShouldReturnCorrectOrderAndLoad() {
        // Arrange
        // Note: locA (1,1) est plus proche de (0,0) que locB (5,5)
        // dist(0,0 -> 1,1) approx 157km
        // dist(0,0 -> 5,5) approx 785km
        // Donc l'ordre doit être A, puis B.
        RouteRequest request = new RouteRequest(depot, List.of(locA, locB), List.of(livreur));

        // Act
        OptimizedRouteResponse response = routeOptimizationService.optimizeRoutes(request, "NearestNeighbor");

        // Assert
        assertNotNull(response);
        assertEquals("NearestNeighbor", response.getAlgorithm());
        assertEquals(1, response.getTournees().size());

        OptimizedRouteResponse.RouteDetails route = response.getTournees().get(0);
        // L'ordre doit être le plus proche (A) puis le suivant (B)
        assertEquals(List.of("A", "B"), route.getStopOrder());
        // La charge doit être la somme (10 + 20)
        assertEquals(30.0, route.getRouteLoad());
        assertTrue(route.getRouteDistanceKm() > 0);
    }

    @Test
    @DisplayName("ClarkeWright devrait retourner une tournée contenant A et B")
    void optimizeRoutes_ClarkeWright_ShouldReturnAllStopsAndLoad() {
        // Arrange
        RouteRequest request = new RouteRequest(depot, List.of(locA, locB), List.of(livreur));

        // Act
        OptimizedRouteResponse response = routeOptimizationService.optimizeRoutes(request, "ClarkeWright");

        // Assert
        assertNotNull(response);
        assertEquals("ClarkeWright", response.getAlgorithm());
        assertEquals(1, response.getTournees().size());

        OptimizedRouteResponse.RouteDetails route = response.getTournees().get(0);
        // ClarkeWright fusionne les tournées. L'ordre peut varier, mais les deux arrêts doivent être présents.
        assertThat(route.getStopOrder(), containsInAnyOrder("A", "B"));
        // La charge doit être la somme (10 + 20)
        assertEquals(30.0, route.getRouteLoad());
        assertTrue(route.getRouteDistanceKm() > 0);
    }

    @Test
    @DisplayName("NearestNeighbor devrait respecter la capacité du véhicule")
    void optimizeRoutes_NearestNeighbor_ShouldRespectCapacity() {
        // Arrange
        // locA (poids 10), locB (poids 20)
        // Le livreur ne peut prendre que 15
        RouteRequest.LivreurData livreurCapaciteLimitee = new RouteRequest.LivreurData("L2", 15.0);
        RouteRequest request = new RouteRequest(depot, List.of(locA, locB), List.of(livreurCapaciteLimitee));

        // Act
        OptimizedRouteResponse response = routeOptimizationService.optimizeRoutes(request, "NearestNeighbor");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTournees().size());

        OptimizedRouteResponse.RouteDetails route = response.getTournees().get(0);

        // L'algorithme prend le plus proche (A, poids 10).
        // Il essaie ensuite B (poids 20), mais 10 + 20 > 15.
        // Donc, la tournée ne doit contenir que A.
        assertEquals(List.of("A"), route.getStopOrder());
        assertEquals(10.0, route.getRouteLoad());
    }

    @Test
    @DisplayName("Devrait lever IllegalArgumentException pour un algorithme inconnu")
    void optimizeRoutes_ShouldThrowException_ForUnsupportedAlgorithm() {
        // Arrange
        RouteRequest request = new RouteRequest(depot, List.of(locA, locB), List.of(livreur));
        String algoInconnu = "Dijkstra";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            routeOptimizationService.optimizeRoutes(request, algoInconnu);
        });

        assertTrue(exception.getMessage().contains("Algorithme non supporté: " + algoInconnu));
    }
}