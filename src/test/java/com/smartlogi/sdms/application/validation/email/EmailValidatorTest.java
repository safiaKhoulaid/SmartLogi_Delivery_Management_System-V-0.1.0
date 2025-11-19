package com.smartlogi.sdms.application.validation.email;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailValidatorTest {

    private EmailValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }

    @Test
    @DisplayName("isValid devrait retourner VRAI pour un email valide")
    void isValid_ShouldReturnTrue_ForValidEmail() {
        assertTrue(validator.isValid("utilisateur@domaine.com", context));
        assertTrue(validator.isValid("utilisateur.nom@domaine.co.ma", context));
        assertTrue(validator.isValid("test_123@google.fr", context));
    }

    @Test
    @DisplayName("isValid devrait retourner FAUX pour un email invalide")
    void isValid_ShouldReturnFalse_ForInvalidEmail() {
        assertFalse(validator.isValid("utilisateurdomaine.com", context)); // Manque @
        assertFalse(validator.isValid("utilisateur@domaine", context));   // Manque .com/.fr etc.
        assertFalse(validator.isValid("@domaine.com", context));        // Manque nom
        assertFalse(validator.isValid("", context));                  // Vide
    }

    @Test
    @DisplayName("isValid devrait retourner FAUX pour un email null")
    void isValid_ShouldReturnFalse_ForNullEmail() {
        // Le validateur doit être robuste aux nuls (même si @NotBlank le gère)
        // Le Pattern.matcher() lève une NullPointerException si non géré.
        // Le validateur fourni gère cela.
        assertFalse(validator.isValid(null, context));
    }
}