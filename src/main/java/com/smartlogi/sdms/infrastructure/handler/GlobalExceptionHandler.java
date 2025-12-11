package com.smartlogi.sdms.infrastructure.handler;

import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.exception.UserAlreadyExistsException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
// --- AJOUTS IMPORTANTS ---
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
// --- FIN AJOUTS ---
import org.springframework.web.bind.MethodArgumentNotValidException; // (Assurez-vous que cet import existe si vous avez ajouté le handler 400)
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Le record ErrorDetails est défini ici
    public record ErrorDetails(LocalDateTime timestamp, String message, String details) {}

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND); // Renvoie 404
    }

    // --- GESTIONNAIRES AJOUTÉS ---

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), "Accès non autorisé. Vous n'avez pas les droits nécessaires pour effectuer cette action.", request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN); // Renvoie 403
    }

    // (Handler pour MethodArgumentNotValidException - 400 - si vous l'avez ajouté)

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(ValidationException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), exception.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST); // Renvoie 400
    }

    // --- CORRECTION REQUISE ---
    /**
     * Gère les échecs d'authentification (ex: mauvais mot de passe).
     * Intercepte BadCredentialsException (et son parent AuthenticationException).
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorDetails> handleAuthenticationException(Exception exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Échec de l'authentification. Veuillez vérifier vos identifiants.", // Message public
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED); // Renvoie 401
    }
    // --- FIN CORRECTION ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception, WebRequest request) {
        // Idéalement, loguez l'erreur ici
        // log.error("Erreur interne inattendue : ", exception);

        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), "Une erreur interne est survenue. Veuillez contacter le support.", request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR); // Renvoie 500
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleUserAlreadyExists(UserAlreadyExistsException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT); // 409
    }

}