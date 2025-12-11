package com.smartlogi.sdms.infrastructure.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HaversineTest {

    @Test
    @DisplayName("distance devrait retourner 0 pour des coordonnées identiques")
    void distance_ShouldReturnZero_ForSameCoordinates() {
        // Arrange
        double lat = 33.5731; // Casablanca
        double lon = -7.5898;

        // Act
        double dist = Haversine.distance(lat, lon, lat, lon);

        // Assert
        assertEquals(0.0, dist);
    }

    @Test
    @DisplayName("distance devrait calculer la distance entre Casablanca et Rabat")
    void distance_ShouldCalculateCorrectDistance_CasablancaToRabat() {
        // Arrange
        double casaLat = 33.5731;
        double casaLon = -7.5898;
        double rabatLat = 34.0209;
        double rabatLon = -6.8417;

        // Distance attendue (approximative)
        double expectedDistanceKm = 86.0; // Environ 86-87 km

        // Act
        double dist = Haversine.distance(casaLat, casaLon, rabatLat, rabatLon);

        // Assert
        // Utiliser une tolérance (delta) pour les calculs en virgule flottante
        assertEquals(expectedDistanceKm, dist, 2.0); // Tolérance de 2 km
    }
}