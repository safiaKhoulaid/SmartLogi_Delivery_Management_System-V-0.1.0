package com.smartlogi.sdms.application.validation.colis;

import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColisDestinationValidatorTest {

    private ColisDestinationValidator validator;
    private ColisRequestDTO dto;

    // Contexte Mocké
    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new ColisDestinationValidator();
        dto = new ColisRequestDTO();

        // --- CORRECTION : Stubs supprimés de setUp() ---
    }

    @Test
    @DisplayName("isValid devrait retourner VRAI si seul destinataireId est fourni")
    void isValid_ShouldReturnTrue_WhenOnlyIdIsPresent() {
        // Arrange
        dto.setDestinataireId("dest-123");
        dto.setDestinataireInfo(null);

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertTrue(result);
        // Aucun stub 'when' n'est nécessaire ici
    }

    @Test
    @DisplayName("isValid devrait retourner VRAI si seul destinataireInfo est fourni")
    void isValid_ShouldReturnTrue_WhenOnlyInfoIsPresent() {
        // Arrange
        dto.setDestinataireId(null);
        dto.setDestinataireInfo(new DestinataireRequestDTO());

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertTrue(result);
        // Aucun stub 'when' n'est nécessaire ici
    }

    @Test
    @DisplayName("isValid devrait retourner FAUX si les DEUX sont fournis")
    void isValid_ShouldReturnFalse_WhenBothArePresent() {
        // Arrange
        dto.setDestinataireId("dest-123");
        dto.setDestinataireInfo(new DestinataireRequestDTO());

        // --- CORRECTION : Stubs déplacés ici ---
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
        // --- FIN CORRECTION ---

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertFalse(result);

        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(context, times(1)).buildConstraintViolationWithTemplate(contains("mais pas les deux"));
    }

    @Test
    @DisplayName("isValid devrait retourner FAUX si AUCUN n'est fourni")
    void isValid_ShouldReturnFalse_WhenNeitherIsPresent() {
        // Arrange
        dto.setDestinataireId(null);
        dto.setDestinataireInfo(null);

        // --- CORRECTION : Stubs déplacés ici ---
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
        // --- FIN CORRECTION ---

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertFalse(result);

        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(context, times(1)).buildConstraintViolationWithTemplate(contains("soit l'ID"));
    }
}