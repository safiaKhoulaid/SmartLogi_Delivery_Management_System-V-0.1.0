package com.smartlogi.sdms.domain.model.enums;

public enum StatusColis {
    CREE,
    EN_Collecte,// Colis créé par le client expéditeur
    COLLECTE,      // Colis collecté par le livreur
    EN_STOCK,      // Colis stocké en entrepôt
    EN_TRANSIT,    // Colis en cours de livraison
    LIVRE          // Colis livré au destinataire
}
