package com.smartlogi.sdms.domain.model.vo;


import com.smartlogi.sdms.domain.model.enums.TypeVehicule;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public record Vehicule(
        @NotNull(message = "Le type de véhicule est obligatoire.")
        @Enumerated(EnumType.STRING)
        @Column(name = "type_vehicule", nullable = false)
        TypeVehicule type,

        @NotBlank(message = "Le matricule ne peut être vide.")
        @Size(min = 4, max = 15, message = "Le matricule doit contenir entre 4 et 15 caractères.")
        @Pattern(regexp = "^[A-Za-z0-9\\-]+$",
                message = "Le matricule doit contenir uniquement des lettres, des chiffres et des tirets.")
        @Column(name = "matricule", unique = true, nullable = false)
        String matricule,

        @Column(name = "capacite_maximale")
        double capaciteMaximale
) {
    public Vehicule {
        if (matricule != null) {
            matricule = matricule.toUpperCase().trim();
        }
    }
}
