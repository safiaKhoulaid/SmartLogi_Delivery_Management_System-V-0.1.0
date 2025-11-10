package com.smartlogi.sdms.application.dto.mission;

import com.smartlogi.sdms.domain.model.enums.MissionType;
import com.smartlogi.sdms.domain.model.enums.StatutMission;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MissionResponseDTO {

    private String id; // UUID

    private MissionType type;
    private StatutMission statut;
    private Adresse origineAdresse;
    private Adresse destinationAdresse;
    private LocalDateTime datePrevue;
    private LocalDateTime dateEffective;
    private String commentaire;

    // --- Relations ---

    private String livreurId;
    // Ajout d'informations supplémentaires du livreur (ex : nom) pour une réponse plus riche
    private String livreurNomComplet;

    private String colisId;
    // Ajout d'informations supplémentaires du colis (ex : description)
    private String colisDescription;
}