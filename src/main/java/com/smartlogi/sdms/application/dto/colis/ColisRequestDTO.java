package com.smartlogi.sdms.application.dto.colis;

import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.validation.colis.ColisDestinataireValid;
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import com.smartlogi.sdms.domain.model.vo.Poids;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@ColisDestinataireValid
@Data
@NoArgsConstructor
public class ColisRequestDTO {

    // --- Expéditeur (Obligatoire) ---
    @NotNull(message = "L'ID de l'expéditeur est obligatoire.")
    private String expediteurId;


    // 1. Destinataire existant : ID de l'entité Destinataire
    private String destinataireId;

    // 2. Nouveau destinataire : Informations pour la création
    @Valid
    private DestinataireRequestDTO destinataireInfo;

    // --- Détails du Colis ---

    // La description peut être vide (pas de @NotBlank), mais ici, elle est un String.
    // Si la description est essentielle, ajoutez @NotBlank.
    private String description;

    @NotNull(message = "Le poids est obligatoire.")
    private Poids poids;

    private String villeDestination;

    private LocalDateTime dateCreation;

    private PriorityColis priority;

    private StatusColis status;


}