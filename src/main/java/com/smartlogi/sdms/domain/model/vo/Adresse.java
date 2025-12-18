package com.smartlogi.sdms.domain.model.vo;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public record Adresse(

        @NotBlank(message = "Le numéro de rue est obligatoire.")
        @Size(max = 20, message = "Le numéro de rue ne doit pas dépasser 20 caractères.")
        String numero,

        @NotBlank(message = "Le nom de la rue est obligatoire.")
        @Size(max = 50, message = "Le nom de la rue ne doit pas dépasser 50 caractères.")
        String rue,

        @NotBlank(message = "La ville de destination est obligatoire.")
        @Size(max = 30, message = "Le nom de la ville ne doit pas dépasser 30 caractères.")
        String ville,

        @Pattern(regexp = "^\\d{5}$", message = "Code postal invalide (doit contenir 5 chiffres).")
        @NotBlank(message = "Le code postal est obligatoire.")
        @Size(max = 5, message = "Le code postal ne doit pas dépasser 5 caractères.")
        String codePostal,

        @NotBlank(message = "Le pays est obligatoire.")
        @Size(max = 20, message = "Le nom du pays ne doit pas dépasser 20 caractères.")
        String pays,

        Double latitude,
        Double longitude
) {
    /**
     * Constructeur Compact : On tolère les NULL pour que Hibernate ne plante pas.
     */
    public Adresse {
        // حيدنا Objects.requireNonNull باش Hibernate يقدر يشارجي الداتا القديمة

        // 1. Normalisation (Safe Trim & Uppercase)
        if (numero != null) {
            numero = numero.trim();
        }

        if (rue != null) {
            rue = rue.trim().toUpperCase();
        }

        if (ville != null) {
            ville = ville.trim().toUpperCase();
        }

        if (codePostal != null) {
            codePostal = codePostal.trim();
        }

        if (pays == null || pays.isBlank()) {
            pays = "MAROC"; // Default
        } else {
            pays = pays.trim().toUpperCase();
        }

        // latitude & longitude كيبقاو كيف ما جاو
    }

    // Constructeur alternatif (Reste inchangé)
    public Adresse(String numero, String rue, String ville, String codePostal, String pays) {
        this(numero, rue, ville, codePostal, pays, null, null);
    }

    public String getAdresseComplete() {
        return String.format("%s %s, %s %s, %s",
                numero != null ? numero : "",
                rue != null ? rue : "",
                ville != null ? ville : "",
                codePostal != null ? codePostal : "",
                pays);
    }
}