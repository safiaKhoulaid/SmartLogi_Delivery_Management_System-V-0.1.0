package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.routes.OptimizedRouteResponse;
import com.smartlogi.sdms.application.dto.routes.RouteRequest;
import com.smartlogi.sdms.application.service.RouteOptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteOptimizationController {

    private final RouteOptimizationService routeOptimizationService;

    public RouteOptimizationController(RouteOptimizationService routeOptimizationService) {
        this.routeOptimizationService = routeOptimizationService;
    }

    /**
     * Endpoint pour lancer l'optimisation des tournées.
     * @param request Le corps JSON contenant le dépôt, les livreurs et les missions.
     * @param algorithm L'algorithme à utiliser (NearestNeighbor ou ClarkeWright)
     * @return Un JSON avec les tournées optimisées.
     */
    @PostMapping("/optimized")
    public ResponseEntity<OptimizedRouteResponse> getOptimizedRoutes(
            @RequestBody RouteRequest request,
            @RequestParam(name = "algorithm", defaultValue = "NearestNeighbor") String algorithm) {

        OptimizedRouteResponse response = routeOptimizationService.optimizeRoutes(request, algorithm);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour comparer les deux algorithmes.
     */
    @PostMapping("/compare")
    public ResponseEntity<Map<String, OptimizedRouteResponse>> compareAlgorithms(
            @RequestBody RouteRequest request) {

        OptimizedRouteResponse nnResponse = routeOptimizationService.optimizeRoutes(request, "NearestNeighbor");
        OptimizedRouteResponse cwResponse = routeOptimizationService.optimizeRoutes(request, "ClarkeWright");

        Map<String, OptimizedRouteResponse> comparison = Map.of(
                "NearestNeighbor", nnResponse,
                "ClarkeWright", cwResponse
        );

        return ResponseEntity.ok(comparison);
    }
}