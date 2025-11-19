package com.smartlogi.sdms.application.validation.password;

import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordMatchesValidatorTest {

    private PasswordMatchesValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    private UserRequestRegisterDTO userDTO;

    @BeforeEach
    void setUp() {
        validator = new PasswordMatchesValidator();
        userDTO = new UserRequestRegisterDTO();
    }

    @Test
    @DisplayName("isValid devrait retourner VRAI si les mots de passe correspondent")
    void isValid_ShouldReturnTrue_WhenPasswordsMatch() {
        userDTO.setPassword("Password123!");
        userDTO.setMatchingPassword("Password123!");
        assertTrue(validator.isValid(userDTO, context));
    }

    @Test
    @DisplayName("isValid devrait retourner FAUX si les mots de passe ne correspondent pas")
    void isValid_ShouldReturnFalse_WhenPasswordsDoNotMatch() {
        userDTO.setPassword("Password123!");
        userDTO.setMatchingPassword("DIFFERENT");
        assertFalse(validator.isValid(userDTO, context));
    }
}