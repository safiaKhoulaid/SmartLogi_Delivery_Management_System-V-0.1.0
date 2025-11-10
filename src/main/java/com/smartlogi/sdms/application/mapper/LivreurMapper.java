package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.livreur.LivreurResponseDTO;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LivreurMapper {

    /**
     * Mappe l'entité Livreur vers le DTO de réponse.
     * @param livreur L'entité Livreur.
     * @return Le LivreurResponseDTO.
     */
    @Mapping(target = "id", source = "id")

    // Mappage critique 1 : Concaténation de firstName et lastName
    @Mapping(target = "nomComplet", expression = "java(livreur.getFirstName() + \" \" + livreur.getLastName())")

    // Mappage critique 2 : Conversion de l'Enum StatusLivreur vers la chaîne 'status'
    // MapStruct gère automatiquement la conversion Enum -> String (via name()) si les types diffèrent.
    @Mapping(source = "email", target = "email")

    @Mapping(source="telephone" , target="telephone")

    LivreurResponseDTO toResponseDTO(Livreur livreur);
    List<LivreurResponseDTO> toResponseDTO(List<Livreur> livreurs);

}