package com.smartlogi.sdms.application.validation.colis;

import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ColisDestinationValidator implements ConstraintValidator<ColisDestinataireValid, ColisRequestDTO> {

    @Override
    public boolean isValid(ColisRequestDTO dto, ConstraintValidatorContext context) {

        final boolean isIdPresent = dto.getDestinataireId() != null;
        final boolean isInfoPresent = dto.getDestinataireInfo() != null;

        if (isIdPresent && isInfoPresent) {
            // Ni les deux (pas valide)
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Veuillez spécifier un ID OU les informations d'un nouveau destinataire, mais pas les deux.")
                    .addConstraintViolation();
            return false;
        }

        if (!isIdPresent && !isInfoPresent) {
            // Ni aucun des deux (pas valide)
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Veuillez spécifier soit l'ID d'un destinataire existant, soit les informations d'un nouveau destinataire.")
                    .addConstraintViolation();
            return false;
        }

        // Soit l'un, soit l'autre (Valide)
        return true;
    }
}