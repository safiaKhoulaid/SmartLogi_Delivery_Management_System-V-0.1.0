package com.smartlogi.sdms.domain.model.vo;

import com.smartlogi.sdms.domain.model.enums.UnitePoids;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PoidsTest {

    @Test
    @DisplayName("Devrait créer un poids valide")
    void poids_ShouldCreateValidPoids() {
        // Arrange
        BigDecimal valeur = new BigDecimal("2.500");

        // Act
        Poids poids = new Poids(valeur, UnitePoids.KG);

        // Assert
        // Vérifie la normalisation (stripTrailingZeros)
        assertEquals(new BigDecimal("2.5"), poids.valeur());
        assertEquals(UnitePoids.KG, poids.unite());
        assertEquals("2.5 KG", poids.format());
    }

    @Test
    @DisplayName("Devrait lever une exception si le poids est nul")
    void poids_ShouldThrow_WhenValueIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new Poids(null, UnitePoids.KG);
        });
    }

    @Test
    @DisplayName("Devrait lever une exception si l'unité est nulle")
    void poids_ShouldThrow_WhenUnitIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new Poids(BigDecimal.TEN, null);
        });
    }

    @Test
    @DisplayName("Devrait lever une exception si le poids est zéro ou négatif")
    void poids_ShouldThrow_WhenValueIsZeroOrNegative() {
        // Test Zéro
        Exception eZero = assertThrows(IllegalArgumentException.class, () -> {
            new Poids(BigDecimal.ZERO, UnitePoids.KG);
        });
        assertEquals("Le poids doit être positif", eZero.getMessage());

        // Test Négatif
        Exception eNeg = assertThrows(IllegalArgumentException.class, () -> {
            new Poids(new BigDecimal("-10"), UnitePoids.KG);
        });
        assertEquals("Le poids doit être positif", eNeg.getMessage());
    }
}