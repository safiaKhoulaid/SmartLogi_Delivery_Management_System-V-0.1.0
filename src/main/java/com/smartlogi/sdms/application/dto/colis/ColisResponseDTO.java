package com.smartlogi.sdms.application.dto.colis;

import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import com.smartlogi.sdms.domain.model.vo.Poids;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(access = lombok.AccessLevel.PUBLIC)public class ColisResponseDTO {

    // --- Identification du Colis ---
    private String message;
    private String id; // Type String, car c'est un UUID généré
    private String description;
    private LocalDateTime dateCreation;

    // --- Informations Logistiques (statut et planification) ---
    private StatusColis statut;
    private PriorityColis priorite;
    private String villeDestination;
    private String codeZone; // Nom ou code de la Zone (pour éviter de renvoyer l'objet Zone complet)

    // --- Poids (Aplatissement de la Value Object Poids) ---
    private Poids poids;

    // --- Expéditeur (Informations d'affichage) ---
    private String clientExpediteurId;
    private String clientExpediteurNom; // Nom complet ou juste le Nom/Prénom
    private String clientExpediteurTelephone; // Infos de contact utiles

    // --- Destinataire (Informations d'affichage et de suivi) ---
    private String destinataireId; // Utile pour le suivi par l'API
    private String destinataireNom;
    private String destinatairePrenom;
    private String destinataireAdresse; // L'adresse de livraison complète

    // --- Livreur (Assignation) ---
    private String livreurCollecteId;
    private String livreurCollecteNom;

    // --- Traçabilité (Non obligatoire, mais utile pour le suivi) ---
    private String dernierStatutCommentaire;
    private LocalDateTime dateDernierStatut;
    private String trackingCode  ;


}