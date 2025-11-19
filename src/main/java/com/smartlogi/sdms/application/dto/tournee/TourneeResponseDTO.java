package com.smartlogi.sdms.application.dto.tournee;

import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.domain.model.enums.StatutTournee;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
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