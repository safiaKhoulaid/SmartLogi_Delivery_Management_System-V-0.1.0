package com.smartlogi.sdms.application.dto.tournee;

import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.domain.model.enums.StatutTournee;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TourneeResponseDTO {
    private Long id;
    private LocalDate dateTournee;
    private StatutTournee statut;
    private Double distanceTotaleKm;
    private Double dureeEstimeeHeures;
    private String livreurId;
    private String zoneId;
    private String nomZone;
    private List<ColisResponseDTO> livraisons; // Retourne les détails des colis dans l'ordre
}