package com.smartlogi.sdms.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelephoneTest {

    @Test
    @DisplayName("Devrait normaliser un numéro marocain valide")
    void telephone_ShouldNormalizeValidMarocNumber() {
        // Arrange
        String numero = "6 12 34 56 78";

        // Act
        Telephone telephone = new Telephone(null, numero); // Utilise le code par défaut +212

        // Assert
        assertEquals("+212", telephone.codePays());
        assertEquals("612345678", telephone.nombre());
        assertEquals("+212612345678", telephone.getnombreComplet());
    }

    @Test
    @DisplayName("Devrait lever une exception pour un numéro non-marocain")
    void telephone_ShouldThrow_WhenNotMarocNumber() {
        // --- CORRECTION ---
        // Le code
        // DOIT rejeter les numéros non-marocains.
        assertThrows(IllegalArgumentException.class, () -> {
            new Telephone("+33", "612345678");
        });
        // --- FIN CORRECTION ---
    }

    @Test
    @DisplayName("Devrait lever une exception si le numéro est invalide (trop court)")
    void telephone_ShouldThrow_WhenNumberIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Telephone(null, "12345");
        }, "Format de numéro de téléphone invalide");
    }

    @Test
    @DisplayName("Devrait lever une exception si le numéro est null")
    void telephone_ShouldThrow_WhenNumberIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new Telephone(null, null);
        });
    }
}