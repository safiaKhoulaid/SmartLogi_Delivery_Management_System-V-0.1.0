package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.tournee.TourneeResponseDTO;
import com.smartlogi.sdms.domain.model.entity.Tournee;
import jakarta.transaction.Transactional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
@Transactional
@Mapper(componentModel = "spring", uses = {ColisMapper.class})
public interface TourneeMapper {

    @Mappings({
            @Mapping(source = "livreur.id", target = "livreurId"),
            @Mapping(source = "zone.id", target = "zoneId"),
            @Mapping(source = "zone.nom", target = "nomZone"),
            @Mapping(source = "livraisons", target = "livraisons") // "livraisons" est le nom du champ dans l'entité
    })
    TourneeResponseDTO toResponseDTO(Tournee tournee);
}