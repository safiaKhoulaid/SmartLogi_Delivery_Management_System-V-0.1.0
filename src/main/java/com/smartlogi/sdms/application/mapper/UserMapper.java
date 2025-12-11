package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.dto.user.DestinataireResponseDTO; // AJOUT
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.model.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // ... (vos mappages existants) ...
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "prenom", target = "firstName")
    @Mapping(source = "nom", target = "lastName")
    @Mapping(target = "codeClient", ignore = true)
    ClientExpediteur toClientExpediteur(UserRequestRegisterDTO dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "clientExpediteur", ignore = true)
    @Mapping(source = "prenom", target = "firstName")
    @Mapping(source = "nom", target = "lastName")
    Destinataire toDestinataireEntity(DestinataireRequestDTO dto);


    // --- AJOUT DE LA NOUVELLE MÉTHODE ---
    /**
     * Mappe l'entité Destinataire (qui hérite de BaseUser) vers le DTO de réponse.
     */
    @Mapping(source = "firstName", target = "prenom")
    @Mapping(source = "lastName", target = "nom")
    DestinataireResponseDTO toResponseDTO(Destinataire destinataire);
}