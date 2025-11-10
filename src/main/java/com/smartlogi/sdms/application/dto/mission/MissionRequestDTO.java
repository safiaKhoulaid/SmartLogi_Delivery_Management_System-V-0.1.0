package com.smartlogi.sdms.application.dto.mission;

import com.smartlogi.sdms.domain.model.enums.MissionType;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MissionRequestDTO {

    // --- Détails de la Mission ---

    @NotNull(message = "Le type de mission est obligatoire (COLLECTE ou LIVRAISON).")
    private MissionType type;

    @NotNull(message = "L'adresse d'origine est obligatoire.")
//    @Size(max = 255, message = "L'adresse d'origine doit contenir numero , rue , ville .")
    private Adresse origineAdresse;

    @NotNull(message = "L'adresse de destination est obligatoire.")
//    @Size(max = 255, message = "L'adresse d'origine doit contenir numero , rue , ville .")
    private Adresse destinationAdresse;

    @NotNull(message = "La date prévue d'exécution est obligatoire.")
    private LocalDateTime datePrevue;

    @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères.")
    private String commentaire; // Optionnel

    // --- Relations (IDs) ---

    @NotBlank(message = "L'ID du livreur est obligatoire.")
    private String livreurId; // L'ID de l'entité Mission est un String (UUID).

    @NotNull(message = "L'ID du colis est obligatoire.")
    private String colisId; // Hypothèse : l'ID de Colis est un Long
}