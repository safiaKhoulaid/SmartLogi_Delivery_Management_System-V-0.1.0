package com.smartlogi.sdms.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdresseTest {
    // فـ AdresseTest.java، حيدي هاد الـ test أو بدليه حيت مابقاش كايعطي Exception
    @Test
    @DisplayName("Ne devrait plus lever d'exception pour les champs nulls")
    void adresse_ShouldNotThrow_WhenFieldsAreNull() {
        assertDoesNotThrow(() -> new Adresse(null, null, null, null, null));
    }
    @Test
    @DisplayName("Devrait normaliser les champs (MAJUSCULES et trim)")
    void adresse_ShouldNormalizeFields() {
        // Arrange
        String numero = " 123b ";
        String rue = " rue de test ";
        String ville = " casablanca ";
        String pays = " maroc ";

        // Act
        Adresse adresse = new Adresse(numero, rue, ville, "20000", pays, 33.5, -7.6);

        // Assert
        assertEquals("123b", adresse.numero());
        assertEquals("RUE DE TEST", adresse.rue());
        assertEquals("CASABLANCA", adresse.ville());
        assertEquals("MAROC", adresse.pays());
    }

    @Test
    @DisplayName("Devrait mettre 'MAROC' par défaut si le pays est null ou vide")
    void adresse_ShouldDefaultCountry_WhenNull() {
        // Act
        Adresse adresseNull = new Adresse("123", "Rue", "Ville", "10000", null, 33.5, -7.6);
        Adresse adresseVide = new Adresse("123", "Rue", "Ville", "10000", " ", 33.5, -7.6);

        // Assert
        assertEquals("MAROC", adresseNull.pays());
        assertEquals("MAROC", adresseVide.pays());
    }

    @Test
    @DisplayName("Devrait lever NullPointerException si un champ obligatoire est null")
    void adresse_ShouldThrow_WhenRequiredFieldsAreNull() {
        // (Le record gère cela via Objects.requireNonNull)
        assertThrows(NullPointerException.class, () -> {
            new Adresse(null, "Rue", "Ville", "10000", "Pays", null, null);
        }, "Le numéro de rue ne peut être nul.");

        assertThrows(NullPointerException.class, () -> {
            new Adresse("123", null, "Ville", "10000", "Pays", null, null);
        }, "Le nom de la rue ne peut être nul.");
    }
}