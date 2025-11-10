package com.smartlogi.sdms.domain.model.vo;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.Objects;


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

        // Code postal marocain typique de CINQ chiffres.
        @Pattern(regexp = "^\\d{5}$", message = "Code postal invalide (doit contenir 5 chiffres).")
        @NotBlank(message = "Le code postal est obligatoire.")
        @Size(max = 5, message = "Le code postal ne doit pas dépasser 5 caractères.")
        String codePostal,

        @NotBlank(message = "Le pays est obligatoire.")
        @Size(max = 20, message = "Le nom du pays ne doit pas dépasser 20 caractères.")
        String pays,

        // ⬇️ --- AJOUTS OBLIGATOIRES --- ⬇️
        Double latitude,
        Double longitude
        // ⬆️ --- FIN DES AJOUTS --- ⬆️

) {
    /**
     * Constructeur canonique explicite pour la normalisation et la validation des invariants.
     */
    public Adresse {

        // 1. Validation des invariants
        Objects.requireNonNull(numero, "Le numéro de rue ne peut être nul.");
        Objects.requireNonNull(rue, "Le nom de la rue ne peut être nul.");
        Objects.requireNonNull(ville, "Le nom de la ville ne peut être nul.");
        Objects.requireNonNull(codePostal, "Le code postal ne peut être nul.");

        // 2. Normalisation des champs
        numero = numero.trim();
        rue = rue.trim().toUpperCase();
        ville = ville.trim().toUpperCase();
        codePostal = codePostal.trim();

        if (pays == null || pays.isBlank()) {
            pays = "MAROC"; // Valeur par défaut
        } else {
            pays = pays.trim().toUpperCase();
        }

        // Les champs latitude et longitude sont assignés implicitement (pas de trim/uppercase)
    }

    /**
     * Constructeur alternatif pour créer une Adresse sans coordonnées (elles seront null).
     */
    public Adresse(String numero, String rue, String ville, String codePostal, String pays) {
        this(numero, rue, ville, codePostal, pays, null, null);
    }

    /**
     * Retourne une représentation lisible de l'adresse.
     * @return L'adresse formatée.
     */
    public String getAdresseComplete() {
        return String.format("%s %s, %s %s, %s", numero, rue, ville, codePostal, pays);
    }
}