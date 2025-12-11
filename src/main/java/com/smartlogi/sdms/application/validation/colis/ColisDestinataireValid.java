package com.smartlogi.sdms.application.validation.colis;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Valide que, dans un DTO de création de colis, soit destinataireId,
 * soit destinataireInfo est fourni, mais pas les deux.
 */
@Target({ElementType.TYPE}) // L'annotation s'applique à la classe entière
@Retention(RetentionPolicy.RUNTIME) // Conserver l'annotation au runtime
@Constraint(validatedBy = ColisDestinationValidator.class) // Lien vers le validateur
@Documented
public @interface ColisDestinataireValid {

    // Message d'erreur par défaut (peut être surchargé dans l'usage)
    String message() default "Le destinataire doit être spécifié soit par un ID existant, soit par de nouvelles informations, mais pas les deux.";

    // Groupes de validation (standard Jakarta Validation)
    Class<?>[] groups() default {};

    // Métadonnées
    Class<? extends Payload>[] payload() default {};
}