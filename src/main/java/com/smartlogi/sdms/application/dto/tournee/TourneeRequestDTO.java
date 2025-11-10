package com.smartlogi.sdms.application.dto.tournee;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TourneeRequestDTO {

    @NotNull(message = "La date de la tournée ne peut pas être nulle.")
    private LocalDate dateTournee;

    @NotNull(message = "L'ID du livreur ne peut pas être nul.")
    private String livreurId;

    @NotNull(message = "L'ID de la zone ne peut pas être nul.")
    private String zoneId;

    @NotEmpty(message = "La liste des IDs de colis ne peut pas être vide.")
    private List<String> colisIds; // Les IDs des colis à optimiser

    @NotNull(message = "L'algorithme ne peut pas être nul.")
    private String algorithme; // Ex: "NearestNeighbor" ou "ClarkeWright"
}