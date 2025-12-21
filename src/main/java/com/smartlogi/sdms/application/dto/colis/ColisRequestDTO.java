package com.smartlogi.sdms.application.dto.colis;

import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.validation.colis.ColisDestinataireValid;
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import com.smartlogi.sdms.domain.model.vo.Poids;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@ColisDestinataireValid
@Getter
@Setter
@NoArgsConstructor
public class ColisRequestDTO {

    // --- Expéditeur (Obligatoire) ---
    private String expediteurId;


    // 1. Destinataire existant : ID de l'entité Destinataire
    private String destinataireId;

    // 2. Nouveau destinataire : Informations pour la création
    @Valid
    private DestinataireRequestDTO destinataireInfo;

    // --- Détails du Colis ---

    private String description;

    @NotNull(message = "Le poids est obligatoire.")
    private Poids poids;

    private String villeDestination;

    private LocalDateTime dateCreation;

    private PriorityColis priority;

    private StatusColis status;


}