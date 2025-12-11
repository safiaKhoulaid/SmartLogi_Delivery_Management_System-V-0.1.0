package com.smartlogi.sdms.application.dto.routes;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor // This constructor requires: (LocationDTO, List<LocationDTO>, List<LivreurData>)
public class RouteRequest {

    private LocationDTO depot;
    private List<LocationDTO> locations;

    // ⬇️ --- LA CORRECTION EST ICI --- ⬇️
    // Votre fichier a probablement "private List<LocationDTO> livreurs;"
    // Il doit être:
    private List<LivreurData> livreurs;
    // ⬆️ --- FIN DE LA CORRECTION --- ⬆️


    /**
     * Données sur le livreur et son véhicule pour cette requête
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LivreurData {
        private String livreurId;
        private double capaciteVehicule; // (en double, comme nous l'avons corrigé)
    }
}