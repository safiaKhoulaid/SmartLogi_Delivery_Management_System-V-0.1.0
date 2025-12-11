package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.routes.LocationDTO;
import com.smartlogi.sdms.application.dto.routes.OptimizedRouteResponse;
import com.smartlogi.sdms.application.dto.routes.RouteRequest;
import com.smartlogi.sdms.infrastructure.utils.Haversine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RouteOptimizationService {

    private static final double VITESSE_MOYENNE_KMH = 40.0;

    /**
     * Méthode principale pour calculer les tournées optimisées.
     */
    public OptimizedRouteResponse optimizeRoutes(RouteRequest request, String algorithm) {
        if ("NearestNeighbor".equalsIgnoreCase(algorithm)) {
            return solveNearestNeighbor(request);
        } else if ("ClarkeWright".equalsIgnoreCase(algorithm)) {
            return solveClarkeWright(request);
        } else {
            throw new IllegalArgumentException("Algorithme non supporté: " + algorithm);
        }
    }

    // --- Matrice des Distances ---
    private double[][] calculateDistanceMatrix(LocationDTO depot, List<LocationDTO> locations) {
        int size = locations.size() + 1; // +1 pour le dépôt
        double[][] matrix = new double[size][size];
        List<LocationDTO> allPoints = new ArrayList<>();
        allPoints.add(depot);
        allPoints.addAll(locations);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                LocationDTO p1 = allPoints.get(i);
                LocationDTO p2 = allPoints.get(j);
                // On suppose que Haversine.distance retourne un double
                matrix[i][j] = Haversine.distance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
            }
        }
        return matrix;
    }

    // --- Algorithme 1: Nearest Neighbor (Plus Proche Voisin) ---
    private OptimizedRouteResponse solveNearestNeighbor(RouteRequest request) {
        List<OptimizedRouteResponse.RouteDetails> routes = new ArrayList<>();
        Set<LocationDTO> unvisited = new HashSet<>(request.getLocations());

        String livreurId = request.getLivreurs().isEmpty() ? "LIVREUR_DEFAULT" : request.getLivreurs().get(0).getLivreurId();
        // Capacite est un double (basé sur le code précédent du TourneeService)
        double capacite = request.getLivreurs().isEmpty() ? Double.MAX_VALUE : request.getLivreurs().get(0).getCapaciteVehicule();

        OptimizedRouteResponse.RouteDetails route = OptimizedRouteResponse.RouteDetails.builder()
                .livreurId(livreurId)
                .stopOrder(new ArrayList<>())
                .build();

        LocationDTO currentLocation = request.getDepot();

        // --- CORRECTIONS ICI ---
        double currentLoad = 0.0;
        double currentDistance = 0.0;
        // --- FIN CORRECTIONS ---

        while (!unvisited.isEmpty()) {
            LocationDTO nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (LocationDTO loc : unvisited) {
                // --- CORRECTIONS ICI ---
                // 1. Haversine retourne un double
                double dist = Haversine.distance(currentLocation.getLatitude(), currentLocation.getLongitude(), loc.getLatitude(), loc.getLongitude());

                // 2. Convertir le poidsDemande (BigDecimal) en double pour la comparaison
                double poidsDemandeDouble = loc.getPoidsDemande() != null ? loc.getPoidsDemande().doubleValue() : 0.0;

                // 3. Comparaison de doubles
                if (dist < minDistance && (currentLoad + poidsDemandeDouble <= capacite)) {
                    minDistance = dist;
                    nearest = loc;
                }
                // --- FIN CORRECTIONS ---
            }

            if (nearest == null) {
                // Soit plus de clients, soit la capacité est atteinte pour les clients restants
                break;
            }

            // --- CORRECTIONS ICI ---
            currentDistance += minDistance;
            // 4. Ajouter la valeur double du BigDecimal
            currentLoad += nearest.getPoidsDemande() != null ? nearest.getPoidsDemande().doubleValue() : 0.0;
            // --- FIN CORRECTIONS ---

            currentLocation = nearest;
            route.getStopOrder().add(nearest.getId());
            unvisited.remove(nearest);
        }

        // Retour au dépôt
        currentDistance += Haversine.distance(currentLocation.getLatitude(), currentLocation.getLongitude(), request.getDepot().getLatitude(), request.getDepot().getLongitude());

        route.setRouteDistanceKm(currentDistance);
        route.setRouteEstimatedTimeHours(currentDistance / VITESSE_MOYENNE_KMH);
        route.setRouteLoad(currentLoad);
        routes.add(route);

        return OptimizedRouteResponse.builder()
                .algorithm("NearestNeighbor")
                .totalDistanceKm(currentDistance)
                .totalEstimatedTimeHours(currentDistance / VITESSE_MOYENNE_KMH)
                .tournees(routes) // Nom du champ corrigé
                .build();
    }


    // --- Algorithme 2: Clarke-Wright Savings (Économies) ---
    private OptimizedRouteResponse solveClarkeWright(RouteRequest request) {

        LocationDTO depot = request.getDepot();
        List<LocationDTO> locations = request.getLocations();
        int n = locations.size();

        // 1. Calculer les "Savings" (Économies)
        List<Saving> savings = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                LocationDTO locI = locations.get(i);
                LocationDTO locJ = locations.get(j);

                double distDepotI = Haversine.distance(depot.getLatitude(), depot.getLongitude(), locI.getLatitude(), locI.getLongitude());
                double distDepotJ = Haversine.distance(depot.getLatitude(), depot.getLongitude(), locJ.getLatitude(), locJ.getLongitude());
                double distIJ = Haversine.distance(locI.getLatitude(), locI.getLongitude(), locJ.getLatitude(), locJ.getLongitude());

                double saving = distDepotI + distDepotJ - distIJ;
                savings.add(new Saving(locI, locJ, saving));
            }
        }

        // 2. Trier les économies par ordre décroissant
// Dans la méthode solveClarkeWright:
        savings.sort(Comparator.comparingDouble((Saving saving) -> saving.getAmount()).reversed());        // 3. Initialiser les tournées
        Map<LocationDTO, List<LocationDTO>> routesMap = new HashMap<>();
        for (LocationDTO loc : locations) {
            routesMap.put(loc, new ArrayList<>(List.of(loc)));
        }

        // 4. Fusionner les tournées (Simplifié)
        for (Saving s : savings) {
            LocationDTO i = s.getLocI();
            LocationDTO j = s.getLocJ();

            List<LocationDTO> routeI = routesMap.get(i);
            List<LocationDTO> routeJ = routesMap.get(j);

            if (routeI != routeJ) {
                // TODO: Ajouter la vérification de capacité en utilisant .doubleValue()
                routeI.addAll(routeJ);
                for (LocationDTO locInJ : routeJ) {
                    routesMap.put(locInJ, routeI);
                }
            }
        }

        // 5. Formater la sortie
        Set<List<LocationDTO>> uniqueRoutes = new HashSet<>(routesMap.values());
        List<OptimizedRouteResponse.RouteDetails> finalRoutes = new ArrayList<>();

        List<RouteRequest.LivreurData> livreurs = request.getLivreurs();
        int livreurIndex = 0;
        double totalDistance = 0;

        for (List<LocationDTO> routeStops : uniqueRoutes) {
            if (livreurIndex >= livreurs.size()) break;

            String livreurId = livreurs.get(livreurIndex++).getLivreurId();
            double routeDist = 0;
            double routeLoad = 0.0; // --- CORRECTION (Initialiser) ---

            LocationDTO lastStop = depot;
            List<String> stopOrder = new ArrayList<>();

            for (LocationDTO stop : routeStops) {
                routeDist += Haversine.distance(lastStop.getLatitude(), lastStop.getLongitude(), stop.getLatitude(), stop.getLongitude());

                // --- CORRECTION ICI ---
                // 1. Convertir le poidsDemande (BigDecimal) en double
                routeLoad += stop.getPoidsDemande() != null ? stop.getPoidsDemande().doubleValue() : 0.0;
                // --- FIN CORRECTION ---

                stopOrder.add(stop.getId());
                lastStop = stop;
            }
            // Retour au dépôt
            routeDist += Haversine.distance(lastStop.getLatitude(), lastStop.getLongitude(), depot.getLatitude(), depot.getLongitude());
            totalDistance += routeDist;

            finalRoutes.add(OptimizedRouteResponse.RouteDetails.builder()
                    .livreurId(livreurId)
                    .stopOrder(stopOrder)
                    .routeDistanceKm(routeDist)
                    .routeEstimatedTimeHours(routeDist / VITESSE_MOYENNE_KMH)
                    .routeLoad(routeLoad)
                    .build());
        }

        return OptimizedRouteResponse.builder()
                .algorithm("ClarkeWright")
                .totalDistanceKm(totalDistance)
                .totalEstimatedTimeHours(totalDistance / VITESSE_MOYENNE_KMH)
                .tournees(finalRoutes) // Nom du champ corrigé
                .build();
    }

    // Classe interne pour l'algorithme Clarke-Wright
    @Getter
    @AllArgsConstructor
    private static class Saving {
        private LocationDTO locI;
        private LocationDTO locJ;
        private double amount;
    }
}