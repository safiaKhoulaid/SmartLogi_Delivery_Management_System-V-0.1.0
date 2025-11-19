package com.smartlogi.sdms.application.service.email;

import com.smartlogi.sdms.application.service.EmailService;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.Mission;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailExpediteurEventTest {

    @Mock
    private EmailService emailService; // La dépendance à mocker

    @InjectMocks
    private EmailExpediteurEvent emailExpediteurEvent; // Le service à tester

    private Livreur livreur;
    private Mission mission;
    private ClientExpediteur expediteur;

    @BeforeEach
    void setUp() {
        livreur = Livreur.builder()
                .firstName("LivreurPrénom")
                .lastName("LivreurNom")
                .build();

        expediteur = ClientExpediteur.builder()
                .firstName("ClientPrénom")
                .lastName("ClientNom")
                .email("client@test.com")
                .build();

        Colis colis = Colis.builder()
                .clientExpediteur(expediteur)
                .build();

        mission = Mission.builder()
                .colis(colis)
                .datePrevue(LocalDateTime.of(2025, 10, 20, 14, 30))
                .build();
    }

    @Test
    void testNotifyCollecte() throws MessagingException {
        // Arrange
        // Simule la méthode void sendTemplateEmail
        doNothing().when(emailService).sendTemplateEmail(anyString(), anyString(), anyString(), anyString());

        // Act
        emailExpediteurEvent.notifyCollecte(livreur, mission);

        // Assert
        // Vérifie que emailService.sendTemplateEmail a été appelé 1 fois
        // avec les bons arguments
        verify(emailService, times(1)).sendTemplateEmail(
                eq("client@test.com"), // (to)
                eq("Collecte de votre colis"), // (subject)
                eq("ClientPrénomClientNom"), // (name)
                // --- CORRECTION : Doit correspondre aux 2 espaces du fichier source ---
                contains("LivreurNom  LivreurPrénom") // (message)
        );
    }
}