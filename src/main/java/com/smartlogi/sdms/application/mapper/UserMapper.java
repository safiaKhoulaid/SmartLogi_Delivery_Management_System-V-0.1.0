package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.model.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // --- 1. Correction du mappage ClientExpediteur (Utilisation de la convention MapStruct) ---

    // Note: Le DTO UserRequestRegisterDTO est mappé vers ClientExpediteur (qui hérite de BaseUser)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "prenom", target = "firstName") // Correction: prenom -> firstName
    @Mapping(source = "nom", target = "lastName")     // Correction: nom -> lastName
    @Mapping(target = "codeClient", ignore = true) // Le code client est généré ailleurs
    // Les autres champs (email, telephone, adresse) sont mappés implicitement si les noms correspondent.
    ClientExpediteur toClientExpediteur(UserRequestRegisterDTO dto);


    // --- 2. Correction du mappage Destinataire (Pour résoudre l'erreur de compilation) ---

    // Map le DTO vers l'entité Destinataire (utilisé pour créer un nouveau destinataire dans ColisService)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", constant = "USER")

    // CORRECTION CRITIQUE: L'entité Destinataire n'a plus le champ expediteurId (supprimé dans Destinataire.java)
    // Elle a maintenant la relation clientExpediteur, que le service doit définir.
    @Mapping(target = "clientExpediteur", ignore = true) // Le service doit définir cette relation obligatoire

    @Mapping(source = "prenom", target = "firstName") // Mappage français -> anglais
    @Mapping(source = "nom", target = "lastName")     // Mappage français -> anglais

    Destinataire toDestinataireEntity(DestinataireRequestDTO dto);
}