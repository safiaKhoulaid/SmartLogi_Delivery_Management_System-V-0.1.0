package com.smartlogi.sdms.domain.exception;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

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


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), "Accès non autorisé. Vous n'avez pas les droits nécessaires pour effectuer cette action.", request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN); // Renvoie 403
    }

    // --- 👇 AJOUT POUR LE 400 BAD REQUEST (Validation) ---

    /**
     * Gère les erreurs de validation métier (ex: ID manquant, règles non respectées).
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(ValidationException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), exception.getMessage(), // Message métier (ex: "L'ID de l'expéditeur est obligatoire.")
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST); // Renvoie 400
    }

    // --- 👇 AJOUT POUR LE 500 INTERNAL SERVER ERROR (Générique) ---

    /**
     * Gère toutes les autres exceptions non prévues (ex: NullPointerException).
     * C'est le "filet de sécurité".
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception, WebRequest request) {
        // Loggez l'exception complète pour le débogage
        // (votre logger @Slf4j le fera si vous l'ajoutez ici)

        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), "Une erreur interne est survenue. Veuillez contacter le support.", request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR); // Renvoie 500
    }


    // Vous avez déjà cette classe interne
    public record ErrorDetails(LocalDateTime timestamp, String message, String details) {
    }

}