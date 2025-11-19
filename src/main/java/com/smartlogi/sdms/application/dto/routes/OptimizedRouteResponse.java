package com.smartlogi.sdms.application.dto.routes;

import lombok.*;

import java.util.List;
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizedRouteResponse {
    private String algorithm;
    private double totalDistanceKm;
    private double totalEstimatedTimeHours; // (Ex: distance / 40 km/h)

    // Renommé de "optimizedRoutes" à "tournées"
    private List<RouteDetails> tournees;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteDetails {
        private String livreurId;
        private List<String> stopOrder; // Liste des IDs de LocationDTO
        private double routeDistanceKm;
        private double routeEstimatedTimeHours;
        private double routeLoad; // Optionnel: Poids total
    }
}