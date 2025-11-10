package com.smartlogi.sdms.infrastructure.handler;


import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice // Rend cette classe capable de capturer des exceptions dans tous les contrôleurs
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND); // Renvoie 404
    }

    // Ajoutez ici d'autres méthodes pour gérer ValidationException, GlobalException, etc.

    // Vous auriez besoin d'une classe ErrorDetails simple pour le retour JSON
    public record ErrorDetails(LocalDateTime timestamp, String message, String details) {}
}