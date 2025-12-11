package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.mission.MissionRequestDTO;
import com.smartlogi.sdms.application.dto.mission.MissionResponseDTO;
import com.smartlogi.sdms.domain.model.entity.Mission;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MissionMapper {

    // =================================================================
    // Mappage Entité → DTO de Réponse
    // =================================================================

    @Mapping(source = "livreur.id", target = "livreurId")
    @Mapping(source = "livreur", target = "livreurNomComplet", qualifiedByName = "mapLivreurNomComplet")
    @Mapping(source = "colis.id", target = "colisId")
    @Mapping(source = "colis.description", target = "colisDescription")
    MissionResponseDTO toResponseDto(Mission mission);


    // =================================================================
    // Mappage DTO de Requête -> Entité (Pour création)
    // =================================================================

    /**
     * Mappe le DTO de requête vers l'entité Mission.
     * Les IDs des relations sont ignorés car les entités complètes (Livreur, Colis)
     * doivent être chargées par le service.
     */
    @Mapping(target = "id", ignore = true) // L'ID est généré par la BDD
    @Mapping(target = "livreur", ignore = true)
    @Mapping(target = "colis", ignore = true)
    @Mapping(target = "dateEffective", ignore = true) // Set par la logique de service

    // Le statut initial est toujours EN_ATTENTE au moment de la création
    @Mapping(target = "statut", expression = "java(com.smartlogi.sdms.domain.model.enums.StatutMission.AFFECTEE)")
    Mission toEntity(MissionRequestDTO requestDTO);

    // =================================================================
    // Méthodes Utilitaires
    // =================================================================

    @Named("mapLivreurNomComplet")
    default String mapLivreurNomComplet(Livreur livreur) {
        if (livreur == null) return null;
        // Supposant que Livreur hérite de BaseUser avec getFirstName/getLastName
        return livreur.getFirstName() + " " + livreur.getLastName();
    }
}