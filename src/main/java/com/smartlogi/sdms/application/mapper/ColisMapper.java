package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring",
        // Importe les classes utiles pour les mappings de Value Objects ou Enums
        uses = {StatusColis.class, PriorityColis.class})
public interface ColisMapper {

    // =================================================================
    // Mappage Entité → DTO de Réponse (Pour affichage/lecture)
    // =================================================================

    // NOTE: Vos mappages commentés sont laissés tels quels.
    // ...

    @Mapping(source = "zoneDestination.codePostal", target = "codeZone")
    // "source" est l'entité Colis. Je suppose que votre entité Zone a un champ "code".
    @Mapping(source = "clientExpediteur.telephone", target = "clientExpediteurTelephone")
    // MapStruct utilisera map(Telephone telephone)
    @Mapping(source = "destinataire.firstName", target = "destinataireNom")
    @Mapping(source = "destinataire.adresse", target = "destinataireAdresse")
    // MapStruct utilisera map(Adresse adresse)
    @Mapping(source = "livreurCollecte.id", target = "livreurCollecteId")
    @Mapping(source = "livreurCollecte.firstName", target = "livreurCollecteNom")
    @Mapping(source = "clientExpediteur", target = "clientExpediteurNom", qualifiedByName = "mapNomComplet")
    // Les champs complexes (dernier statut) sont ignorés
    @Mapping(target = "dernierStatutCommentaire", ignore = true)
    @Mapping(target = "dateDernierStatut", ignore = true)
    public ColisResponseDTO toColisResponseDTO(Colis colis);


    // =================================================================
    // Mappage DTO de Requête -> Entité (Pour création/mise à jour)
    // =================================================================

    // NOTE: Vos mappages commentés sont laissés tels quels.
    // ...

    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "priorite", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "livreurCollecte", ignore = true)
    @Mapping(target = "livreurLivraison", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "historiqueLivraisons", ignore = true)
    @Mapping(target = "clientExpediteur", ignore = true) // Vous les assignez dans le service
    @Mapping(target = "destinataire", ignore = true)     // Vous les assignez dans le service
    @Mapping(target = "zoneDestination", ignore = true)
        // Vous les assignez dans le service
    Colis toEntity(ColisRequestDTO requestDTO);


    // =================================================================
    // Méthodes utilitaires (Qualifiers et Traducteurs)
    // =================================================================

    /**
     * Méthode utilitaire pour mapper le nom complet de l'expéditeur.
     */
    @Named(value = "mapNomComplet")
    default String mapNomComplet(ClientExpediteur client) {
        if (client == null) return null;
        // L'entité BaseUser (héritée par ClientExpediteur) possède 'firstName' et 'lastName'
        return client.getFirstName() + " " + client.getLastName();
    }

    @Named("mapClientExpediteurId")
    default String mapClientExpediteurId(ClientExpediteur client) {
        return client != null ? client.getId() : null;
    }

    /**
     * AJOUTÉ: Traducteur pour MapStruct:
     * Apprend à MapStruct comment convertir un objet Telephone en String.
     */
    default String map(Telephone telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.getnombreComplet();
    }

    /**
     * AJOUTÉ: Traducteur pour MapStruct:
     * Apprend à MapStruct comment convertir un objet Adresse en String.
     */
    default String map(Adresse adresse) {
        if (adresse == null) {
            return null;
        }
        return adresse.getAdresseComplete();
    }

}