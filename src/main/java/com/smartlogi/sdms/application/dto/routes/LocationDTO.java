package com.smartlogi.sdms.application.dto.routes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private String id; // Peut-être l'ID de la Mission ou du Destinataire
    private double latitude;
    private double longitude;
    private BigDecimal poidsDemande; // Optionnel : pour la capacité du véhicule
}