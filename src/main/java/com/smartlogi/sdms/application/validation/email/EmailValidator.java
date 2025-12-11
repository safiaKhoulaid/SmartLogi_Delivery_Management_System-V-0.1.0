package com.smartlogi.sdms.application.validation.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator
        implements ConstraintValidator<EmailValid, String> {

    // --- CORRECTION : Regex remplacée par une version plus standard ---
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Override
    public void initialize(EmailValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return (validateEmail(email));
    }

    private boolean validateEmail(String email) {
        // --- CORRECTION : Ajout d'une vérification de null ---
        if (email == null) {
            return false;
        }
        // --- FIN CORRECTION ---

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}