package com.smartlogi.sdms.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée lorsque la ressource demandée (entité) n'existe pas.
 * Associée au code de statut HTTP 404 (NOT_FOUND).
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Mappe cette exception au statut HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    // Constructeur pour un message personnalisé
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Constructeur optionnel pour une ressource et un ID spécifique
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s non trouvé avec %s : '%s'", resourceName, fieldName, fieldValue));
    }
}