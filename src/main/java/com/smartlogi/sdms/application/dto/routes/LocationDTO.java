package com.smartlogi.sdms.application.dto.routes;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private String id; // Peut-être l'ID de la Mission ou du Destinataire
    private double latitude;
    private double longitude;
    private BigDecimal poidsDemande; // Optionnel : pour la capacité du véhicule
}