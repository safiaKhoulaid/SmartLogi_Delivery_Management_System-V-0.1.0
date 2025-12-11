package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.mission.MissionRequestDTO;
import com.smartlogi.sdms.application.dto.mission.MissionResponseDTO;
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.application.mapper.MissionMapper;
import com.smartlogi.sdms.application.service.email.EmailExpediteurEvent;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.Mission;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.MissionType;
import com.smartlogi.sdms.domain.model.enums.StatutMission;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.repository.ColisRepository;
import com.smartlogi.sdms.domain.repository.LivreurRepository;
import com.smartlogi.sdms.domain.repository.MissionRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    // Mocks pour les dépendances
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private LivreurRepository livreurRepository;
    @Mock
    private ColisRepository colisRepository;
    @Mock
    private MissionMapper missionMapper;
    @Mock
    private ColisMapper colisMapper; // Mocké car injecté, même si non utilisé dans createMission
    @Mock
    private EmailExpediteurEvent emailExpediteurEvent;

    // Le service à tester
    @InjectMocks
    private MissionService missionService;

    // Données de test
    private MissionRequestDTO requestDTO;
    private Livreur livreur;
    private Colis colis;
    private Mission mission;
    private Mission missionSaved;
    private MissionResponseDTO responseDTO;
    private String livreurId = "livreur-123";
    private String colisId = "colis-abc";
    private String expediteurEmail = "expediteur@test.com";

    @BeforeEach
    void setUp() {
        Adresse adresse = new Adresse("1", "Rue Test", "TestVille", "10000", "MAROC", 33.0, -7.0);

        // DTO en entrée
        requestDTO = new MissionRequestDTO();
        requestDTO.setLivreurId(livreurId);
        requestDTO.setColisId(colisId);
        requestDTO.setType(MissionType.COLLECTE);
        requestDTO.setDatePrevue(LocalDateTime.now().plusDays(1));
        requestDTO.setOrigineAdresse(adresse);
        requestDTO.setDestinationAdresse(adresse);

        // Entité Livreur
        livreur = Livreur.builder().id(livreurId).firstName("Livreur").lastName("Test").build();

        // Entité ClientExpediteur (nécessaire pour l'email)
        ClientExpediteur expediteur = ClientExpediteur.builder().id("exp-789").email(expediteurEmail).build();

        // Entité Colis
        colis = Colis.builder().id(colisId).description("Test Colis").clientExpediteur(expediteur).build();

        // Entité Mission (telle que retournée par le mapper)
        mission = Mission.builder()
                .type(MissionType.COLLECTE)
                .datePrevue(requestDTO.getDatePrevue())
                .build();

        // Entité Mission (telle que retournée par le save)
        missionSaved = Mission.builder()
                .id("mission-uuid-456")
                .type(MissionType.COLLECTE)
                .datePrevue(requestDTO.getDatePrevue())
                .livreur(livreur)
                .colis(colis)
                .statut(StatutMission.AFFECTEE)
                .build();

        // DTO de sortie
        responseDTO = new MissionResponseDTO();
        responseDTO.setId("mission-uuid-456");
        responseDTO.setLivreurId(livreurId);
        responseDTO.setColisId(colisId);
    }

    // --- Tests pour createMission ---

    @Test
    @DisplayName("createMission devrait sauvegarder, notifier et retourner le DTO si les données sont valides")
    void createMission_ShouldSaveAndNotify_WhenDataIsValid() throws MessagingException {
        // Arrange
        // 1. Simuler les recherches
        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colis));

        // 2. Simuler les mappers
        when(missionMapper.toEntity(requestDTO)).thenReturn(mission);
        when(missionMapper.toResponseDto(missionSaved)).thenReturn(responseDTO);

        // 3. Simuler la sauvegarde
        ArgumentCaptor<Mission> missionCaptor = ArgumentCaptor.forClass(Mission.class);
        when(missionRepository.save(missionCaptor.capture())).thenReturn(missionSaved);

        // 4. Simuler l'événement email (void)
        doNothing().when(emailExpediteurEvent).notifyCollecte(livreur, missionSaved);

        // Act
        MissionResponseDTO result = missionService.createMission(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("mission-uuid-456", result.getId());

        // Vérifier que la mission capturée a bien été modifiée avant la sauvegarde
        Mission capturedMission = missionCaptor.getValue();
        assertEquals(livreur, capturedMission.getLivreur());
        assertEquals(colis, capturedMission.getColis());
        assertEquals(StatutMission.AFFECTEE, capturedMission.getStatut());

        // Vérifier les appels
        verify(livreurRepository, times(1)).findById(livreurId);
        verify(colisRepository, times(1)).findById(colisId);
        verify(missionRepository, times(1)).save(any(Mission.class));
        verify(emailExpediteurEvent, times(1)).notifyCollecte(livreur, missionSaved);
        verify(missionMapper, times(1)).toResponseDto(missionSaved);
    }

    @Test
    @DisplayName("createMission devrait lever ResourceNotFoundException si le Livreur n'est pas trouvé")
    void createMission_ShouldThrowResourceNotFound_WhenLivreurNotFound() throws MessagingException {
        // Arrange
        // 1. Simuler la recherche de livreur (échec)
        when(livreurRepository.findById(livreurId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            missionService.createMission(requestDTO);
        });

        assertTrue(exception.getMessage().contains("le livreur avec id" + livreurId + "est introuvable"));

        // Vérifier qu'on s'est arrêté avant
        verify(colisRepository, never()).findById(any());
        verify(missionRepository, never()).save(any());
        verify(emailExpediteurEvent, never()).notifyCollecte(any(), any());
    }

    @Test
    @DisplayName("createMission devrait lever ResourceNotFoundException si le Colis n'est pas trouvé")
    void createMission_ShouldThrowResourceNotFound_WhenColisNotFound() throws MessagingException {
        // Arrange
        // 1. Simuler la recherche de livreur (succès)
        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        // 2. Simuler la recherche de colis (échec)
        when(colisRepository.findById(colisId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            missionService.createMission(requestDTO);
        });

        assertTrue(exception.getMessage().contains("colis avec l'id" + colisId + "est introuvable"));

        // Vérifier qu'on s'est arrêté avant la sauvegarde
        verify(missionRepository, never()).save(any());
        verify(emailExpediteurEvent, never()).notifyCollecte(any(), any());
    }

    @Test
    @DisplayName("createMission devrait propager MessagingException si l'envoi d'email échoue")
    void createMission_ShouldPropagateException_WhenEmailFails() throws MessagingException {
        // Arrange
        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colis));
        when(missionMapper.toEntity(requestDTO)).thenReturn(mission);
        when(missionRepository.save(any(Mission.class))).thenReturn(missionSaved);

        // 4. Simuler l'échec de l'email
        doThrow(new MessagingException("Erreur SMTP"))
                .when(emailExpediteurEvent).notifyCollecte(livreur, missionSaved);

        // Act & Assert
        Exception exception = assertThrows(MessagingException.class, () -> {
            missionService.createMission(requestDTO);
        });

        assertEquals("Erreur SMTP", exception.getMessage());

        // Vérifier que la sauvegarde a eu lieu, mais pas le mappage de réponse
        verify(missionRepository, times(1)).save(any(Mission.class));
        verify(missionMapper, never()).toResponseDto(any());
    }
}