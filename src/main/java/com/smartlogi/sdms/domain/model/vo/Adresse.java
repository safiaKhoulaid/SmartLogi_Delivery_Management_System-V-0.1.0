package com.smartlogi.sdms.domain.model.vo;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Value Object représentant une adresse complète.
 */
@Embeddable
public record Adresse(

@NotBlank @Size(max = 20) //numero de rue
String numero,

@NotBlank @Size(max = 50) //le nom de rue
String rue,

@NotBlank @Size(max = 30) //le nom de ville
String ville,

@Pattern(regexp = "\\d{5}", message = "Code postal invalide")
@NotBlank @Size(max = 30) //le code postal de ville
String codePostal,

@NotBlank @Size(max = 20) //le pays
String pays

) {

    // Constructeur compact pour valeurs par défaut et normalisation

    public Adresse {
        if (numero != null) numero = numero.trim();
        if (rue != null) rue = rue.trim().toUpperCase();
        if (ville != null) ville = ville.trim().toUpperCase();
        if (codePostal != null) codePostal = codePostal.trim();
        if (pays == null || pays.isBlank()) {
            pays = "Maroc"; // valeur par défaut
        } else {
            pays = pays.trim();
        }
    }
}
