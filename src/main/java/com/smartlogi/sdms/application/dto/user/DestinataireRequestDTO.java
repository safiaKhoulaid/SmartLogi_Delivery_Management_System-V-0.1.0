package com.smartlogi.sdms.application.dto.user;

import com.smartlogi.sdms.application.validation.email.EmailValid;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import jakarta.validation.Valid; // Ajouté
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Ajouté
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Ajouté pour la désérialisation JSON

@Data
@NoArgsConstructor // Recommandé pour Spring Boot et désérialisation
@AllArgsConstructor
public class DestinataireRequestDTO {

    @NotBlank(message = "Le nom du destinataire est obligatoire.")
    private String nom;

    @NotBlank(message = "Le prénom du destinataire est obligatoire.")
    private String prenom;

    @EmailValid
    private String email;

    // Validation des Value Objects
    @NotNull(message = "Le téléphone est obligatoire.")
    @Valid // Valide les contraintes dans Telephone.java
    private Telephone telephone;

    // Validation des Value Objects
    @NotNull(message = "L'adresse est obligatoire.")
    @Valid // Valide les contraintes dans Adresse.java
    private Adresse adresse;

    /**
     * Indique si l'adresse doit être stockée et proposée pour une utilisation future
     * par le client expéditeur (logique métier).
     */
    private Boolean doitEtreEnregistre = false;
}