package com.smartlogi.sdms.domain.model.vo;

import com.smartlogi.sdms.domain.model.enums.UnitePoids;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object représentant le poids d'un colis ou d'un produit.
 * Immuable et toujours positif.
 */
@Embeddable
public record Poids(

        @NotNull(message = "Le poids ne peut pas être nul")
        @DecimalMin(value = "0.01", inclusive = true, message = "Le poids doit être positif")
        BigDecimal valeur,

        @NotNull(message = "L'unité de poids est obligatoire")
        UnitePoids unite

) {
    // Constructeur compact pour validation supplémentaire
    public Poids {
        Objects.requireNonNull(valeur, "Le poids ne peut pas être nul");
        if (valeur.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le poids doit être positif");
        }
        unite = Objects.requireNonNull(unite, "L'unité de poids est obligatoire");

        // Normalisation (supprimer les zéros inutiles)
        valeur = valeur.stripTrailingZeros();
    }

    // Méthode utilitaire pour affichage
    public String format() {
        return valeur + " " + unite;
    }
}