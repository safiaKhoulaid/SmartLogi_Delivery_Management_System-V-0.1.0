package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.Zone;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Poids;
import com.smartlogi.sdms.domain.model.enums.UnitePoids;
import com.smartlogi.sdms.domain.repository.*;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColisServiceTest {

    @Mock
    private ColisRepository colisRepository;
    @Mock
    private DestinataireRepository destinataireRepository;
    @Mock
    private BaseUserService baseUserService;
    @Mock
    private ColisMapper colisMapper;
    @Mock
    private ZoneRepository zoneRepository;
    @Mock
    private ClientExpediteurRepository clientExpediteurRepository;
    @Mock
    private MissionRepository missionRepository; // Ajouté pour le test de suppression

    // --- Le Service à tester ---
    @InjectMocks
    private ColisService colisService;

    // --- Données de test ---
    private ClientExpediteur expediteur;
    private Destinataire destinataire;
    private Zone zone;
    private ColisRequestDTO requestDTO;
    private Colis colis;
    private Adresse adresse;
    private DestinataireRequestDTO destinataireInfo;

    @BeforeEach
    void setUp() {
        adresse = new Adresse("123", "Rue Test", "CASABLANCA", "20000", "MAROC", 33.5, -7.6);
        expediteur = new ClientExpediteur();
        expediteur.setId("expediteur-123");

        destinataire = new Destinataire();
        destinataire.setId("destinataire-456");
        destinataire.setEmail("destinataire@test.com");
        destinataire.setAdresse(adresse);

        zone = new Zone();
        zone.setId("zone-abc");
        zone.setCodePostal("20000");

        // DTO pour une nouvelle info destinataire
        destinataireInfo = new DestinataireRequestDTO();
        destinataireInfo.setEmail("destinataire@test.com");
        destinataireInfo.setNom("Desti");
        destinataireInfo.setPrenom("Test");
        destinataireInfo.setAdresse(adresse);

        // DTO de Requête principal (Cas: Nouveau destinataire)
        requestDTO = new ColisRequestDTO();
        requestDTO.setExpediteurId("expediteur-123");
        requestDTO.setDestinataireInfo(destinataireInfo);
        requestDTO.setDescription("Test Colis");
        requestDTO.setPoids(new Poids(BigDecimal.ONE, UnitePoids.KG));

        // Entité Colis
        colis = Colis.builder()
                .id("colis-uuid-789")
                .description("Test Colis")
                .clientExpediteur(expediteur)
                .destinataire(destinataire)
                .zoneDestination(zone)
                .statut(StatusColis.CREE)
                .priorite(PriorityColis.NORMALE)
                .dateCreation(LocalDateTime.now())
                .villeDestination("CASABLANCA")
                .build();
    }

    // --- Tests pour createColis ---

    @Test
    void testCreateColis_WithNewDestinataire_ShouldSaveColis() {
        // Arrange
        when(baseUserService.findClientExpediteurById("expediteur-123")).thenReturn(Optional.of(expediteur));
        when(destinataireRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(baseUserService.createDestinataire(any(DestinataireRequestDTO.class), any(ClientExpediteur.class)))
                .thenReturn(destinataire);
        when(zoneRepository.findByVilleAndCodePostal("CASABLANCA", "20000")).thenReturn(Optional.of(zone));
        when(colisMapper.toEntity(any(ColisRequestDTO.class))).thenReturn(colis);
        when(colisRepository.save(any(Colis.class))).thenReturn(colis);

        // Act
        Colis result = colisService.createColis(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(colis.getId(), result.getId());
        assertEquals(StatusColis.CREE, result.getStatut());
        assertEquals(expediteur, result.getClientExpediteur());

        verify(baseUserService, times(2)).findClientExpediteurById("expediteur-123"); // 1 pour le destinataire, 1 pour le colis
        verify(destinataireRepository, times(1)).findByEmail("destinataire@test.com");
        verify(baseUserService, times(1)).createDestinataire(any(), any());
        verify(zoneRepository, times(1)).findByVilleAndCodePostal("CASABLANCA", "20000");
        verify(colisRepository, times(1)).save(colis);
    }

    @Test
    void testCreateColis_WithExistingDestinataire_ShouldUseExisting() {
        // Arrange
        requestDTO.setDestinataireInfo(null);
        requestDTO.setDestinataireId("destinataire-456");

        when(baseUserService.findClientExpediteurById("expediteur-123")).thenReturn(Optional.of(expediteur));
        when(destinataireRepository.findById("destinataire-456")).thenReturn(Optional.of(destinataire));
        when(zoneRepository.findByVilleAndCodePostal("CASABLANCA", "20000")).thenReturn(Optional.of(zone));
        when(colisMapper.toEntity(any(ColisRequestDTO.class))).thenReturn(colis);
        when(colisRepository.save(any(Colis.class))).thenReturn(colis);

        // Act
        Colis result = colisService.createColis(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(destinataire, result.getDestinataire());
        // S'assurer qu'on n'a PAS créé de nouveau destinataire
        verify(baseUserService, never()).createDestinataire(any(), any());
        verify(destinataireRepository, never()).findByEmail(anyString());
    }

    @Test
    void testCreateColis_ShouldThrowValidationException_WhenExpediteurIdIsNull() {
        // Arrange
        requestDTO.setExpediteurId(null);

        // Act & Assert
        Exception e = assertThrows(ValidationException.class, () -> {
            colisService.createColis(requestDTO);
        });
        assertEquals("L'ID de l'expéditeur est obligatoire.", e.getMessage());
    }

    @Test
    void testCreateColis_ShouldThrowResourceNotFound_WhenZoneNotFound() {
        // Arrange
        when(baseUserService.findClientExpediteurById("expediteur-123")).thenReturn(Optional.of(expediteur));
        when(destinataireRepository.findById(anyString())).thenReturn(Optional.of(destinataire));
        requestDTO.setDestinataireInfo(null); // Simplification pour ce test
        requestDTO.setDestinataireId("destinataire-456");

        // Simuler la zone non trouvée
        when(zoneRepository.findByVilleAndCodePostal(anyString(), anyString())).thenReturn(Optional.empty());

        // Act & Assert
        Exception e = assertThrows(ResourceNotFoundException.class, () -> {
            colisService.createColis(requestDTO);
        });
        assertTrue(e.getMessage().contains("Zone logistique introuvable"));
    }

    // --- NOUVEAUX TESTS AJOUTÉS POUR LA COUVERTURE ---

    @Test
    void testCreateColis_ShouldThrowValidationException_WhenBothDestinataireIdAndInfoProvided() {
        // Arrange: Le DTO contient les DEUX champs, ce qui est interdit
        requestDTO.setDestinataireId("un-id-existant");
        // requestDTO.setDestinataireInfo est déjà défini dans setUp()

        // Act & Assert
        Exception e = assertThrows(ValidationException.class, () -> {
            colisService.createColis(requestDTO);
        });
        assertTrue(e.getMessage().contains("EXISTANT OU de NOUVELLES informations, mais pas les deux"));
    }

    @Test
    void testCreateColis_ShouldThrowValidationException_WhenNeitherDestinataireIdNorInfoProvided() {
        // Arrange: Le DTO ne contient AUCUN des deux champs
        requestDTO.setDestinataireId(null);
        requestDTO.setDestinataireInfo(null);

        // Act & Assert
        Exception e = assertThrows(ValidationException.class, () -> {
            colisService.createColis(requestDTO);
        });
        assertEquals("Les informations du destinataire sont obligatoires.", e.getMessage());
    }

    @Test
    void testCreateColis_WithNewInfo_ShouldReuseExistingDestinataireByEmail() {
        // Arrange
        // requestDTO (via setUp) contient destinataireInfo
        when(baseUserService.findClientExpediteurById("expediteur-123")).thenReturn(Optional.of(expediteur));

        // Simuler la DECOUVERTE d'un destinataire existant par email
        when(destinataireRepository.findByEmail("destinataire@test.com")).thenReturn(Optional.of(destinataire));

        when(zoneRepository.findByVilleAndCodePostal("CASABLANCA", "20000")).thenReturn(Optional.of(zone));
        when(colisMapper.toEntity(any(ColisRequestDTO.class))).thenReturn(colis);
        when(colisRepository.save(any(Colis.class))).thenReturn(colis);

        // Act
        Colis result = colisService.createColis(requestDTO);

        // Assert
        // Le destinataire final doit être celui trouvé, pas un nouveau
        assertEquals(destinataire.getId(), result.getDestinataire().getId());
        // S'assurer que le service n'a PAS tenté de créer un nouveau destinataire
        verify(baseUserService, never()).createDestinataire(any(), any());
    }

    @Test
    void testCreateColis_ShouldThrowResourceNotFound_WhenDestinataireIdNotFound() {
        // Arrange
        requestDTO.setDestinataireInfo(null);
        requestDTO.setDestinataireId("id-qui-nexiste-pas");

        // Ligne 253 SUPPRIMÉE : Ce mock n'est pas nécessaire car le code lève une exception avant.
        // when(baseUserService.findClientExpediteurById(anyString())).thenReturn(Optional.of(expediteur));

        // Simuler que l'ID n'est pas trouvé
        when(destinataireRepository.findById("id-qui-nexiste-pas")).thenReturn(Optional.empty());

        // Act & Assert
        Exception e = assertThrows(ResourceNotFoundException.class, () -> {
            colisService.createColis(requestDTO);
        });
        assertEquals("Destinataire existant introuvable", e.getMessage());
    }

    @Test
    void testCreateColis_ShouldThrowResourceNotFound_WhenExpediteurIdNotFound() {
        // Arrange
        requestDTO.setExpediteurId("exp-id-inexistant");

        // Simuler l'expéditeur non trouvé (dès la première vérification)
        when(baseUserService.findClientExpediteurById("exp-id-inexistant")).thenReturn(Optional.empty());

        // Act & Assert
        Exception e = assertThrows(ResourceNotFoundException.class, () -> {
            colisService.createColis(requestDTO);
        });
        // Ce message vient du second appel (interne à la logique du destinataire)
        assertEquals("Client expéditeur introuvable.", e.getMessage());
    }

    // --- Tests pour getColisByClientExpediteurId ---

    @Test
    void testGetColisByClientExpediteurId_ShouldReturnPage() {
        // --- Arrange ---
        String idClient = "expediteur-123";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Colis> colisPage = new PageImpl<>(List.of(colis), pageable, 1);

        when(clientExpediteurRepository.existsById(idClient)).thenReturn(true);
        when(colisRepository.findAllByClientExpediteurId(idClient, pageable)).thenReturn(colisPage);

        // --- DÉPLACEMENT DU MOCK ICI ---
        when(colisMapper.toColisResponseDTO(any(Colis.class))).thenAnswer(invocation -> {
            Colis colisArg = invocation.getArgument(0);
            return ColisResponseDTO.builder()
                    .id(colisArg.getId())
                    .description(colisArg.getDescription())
                    .build(); // Simule un mappage simple
        });
        // --- FIN DU DÉPLACEMENT ---

        // --- Act ---
        Page<ColisResponseDTO> resultPage = colisService.getColisByClientExpediteurId(idClient, pageable);

        // --- Assert ---
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        // Vérifie que le mapper a été appelé par la fonction .map() de Page
        verify(colisMapper, times(1)).toColisResponseDTO(colis);
    }

    @Test
    void testGetColisByClientExpediteurId_ShouldThrowResourceNotFound() {
        // Arrange
        String idClient = "client-inexistant";
        Pageable pageable = PageRequest.of(0, 10);
        when(clientExpediteurRepository.existsById(idClient)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            colisService.getColisByClientExpediteurId(idClient, pageable);
        });
        verify(colisRepository, never()).findAllByClientExpediteurId(anyString(), any());
    }

    // --- Tests pour updateColis ---

    @Test
    void testUpdateColis_ShouldUpdateFields() {
        // --- Arrange ---
        String colisId = "colis-uuid-789";
        ColisRequestDTO updateRequest = new ColisRequestDTO();
        updateRequest.setDescription("Description mise à jour");
        updateRequest.setPriority(PriorityColis.HAUTE);

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colis));
        when(colisRepository.save(any(Colis.class))).thenReturn(colis);

        // --- DÉPLACEMENT DU MOCK ICI ---
        when(colisMapper.toColisResponseDTO(any(Colis.class))).thenAnswer(invocation -> {
            Colis colisArg = invocation.getArgument(0);
            return ColisResponseDTO.builder()
                    .id(colisArg.getId())
                    .description(colisArg.getDescription())
                    .priorite(colisArg.getPriorite())
                    .build(); // Simule un mappage simple
        });
        // --- FIN DU DÉPLACEMENT ---

        // --- Act ---
        ColisResponseDTO response = colisService.updateColis(colisId, updateRequest);

        // --- Assert ---
        assertNotNull(response);
        assertEquals("Description mise à jour", colis.getDescription());
        assertEquals(PriorityColis.HAUTE, colis.getPriorite());
        verify(colisRepository, times(1)).findById(colisId);
        verify(colisRepository, times(1)).save(colis);
    }
    @Test
    void testCreateColis_WithDestinataireInfo_ShouldThrowResourceNotFound_WhenExpediteurNotFound() {
        // Arrange
        // Ce test vérifie l'échec du PREMIER appel à findClientExpediteurById
        // (celui à l'intérieur du bloc if (dto.getDestinataireInfo() != null))

        requestDTO.setExpediteurId("exp-id-inexistant");
        // requestDTO.getDestinataireInfo() n'est PAS null (il est défini dans setUp)
        requestDTO.setDestinataireId(null); // S'assurer que nous sommes dans le bon bloc 'if'

        // Simuler l'échec de la recherche de l'expéditeur
        when(baseUserService.findClientExpediteurById("exp-id-inexistant")).thenReturn(Optional.empty());

        // Act & Assert
        Exception e = assertThrows(ResourceNotFoundException.class, () -> {
            colisService.createColis(requestDTO);
        });

        // Vérifier que c'est bien l'exception de ce bloc
        assertEquals("Client expéditeur introuvable.", e.getMessage());

        // Vérifier que l'on n'a jamais atteint la suite de la logique
        verify(zoneRepository, never()).findByVilleAndCodePostal(anyString(), anyString());
        verify(colisRepository, never()).save(any());
    }
    @Test
    void testUpdateColis_ShouldUpdatePoidsAndStatus() {
        // Arrange
        String colisId = "colis-uuid-789";
        Poids newPoids = new Poids(BigDecimal.valueOf(5.0), UnitePoids.KG);
        ColisRequestDTO updateRequest = new ColisRequestDTO();
        updateRequest.setPoids(newPoids);
        updateRequest.setStatus(StatusColis.EN_STOCK);

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colis));
        when(colisRepository.save(any(Colis.class))).thenReturn(colis);

        // Mock mapper (nécessaire pour le retour)
        when(colisMapper.toColisResponseDTO(any(Colis.class))).thenAnswer(invocation ->
                ColisResponseDTO.builder().build() // Un simple DTO suffit
        );

        // Act
        colisService.updateColis(colisId, updateRequest);

        // Assert
        assertEquals(newPoids, colis.getPoids());
        assertEquals(StatusColis.EN_STOCK, colis.getStatut());
        verify(colisRepository, times(1)).save(colis);
    }

    @Test
    void testUpdateColis_ShouldHandleNullUpdates() {
        // Arrange
        String colisId = "colis-uuid-789";
        String originalDescription = colis.getDescription();
        ColisRequestDTO updateRequest = new ColisRequestDTO();
        updateRequest.setDescription(null); // Ne devrait pas changer
        updateRequest.setPoids(null); // Ne devrait pas changer
        updateRequest.setPriority(null); // Ne devrait pas changer
        updateRequest.setStatus(null); // Ne devrait pas changer

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colis));
        when(colisRepository.save(any(Colis.class))).thenReturn(colis);
        when(colisMapper.toColisResponseDTO(any(Colis.class))).thenAnswer(invocation ->
                ColisResponseDTO.builder().build()
        );

        // Act
        colisService.updateColis(colisId, updateRequest);

        // Assert
        // Vérifier que rien n'a changé
        assertEquals(originalDescription, colis.getDescription());
        assertEquals(StatusColis.CREE, colis.getStatut());
        verify(colisRepository, times(1)).save(colis);
    }


    @Test
    void testUpdateColis_ShouldThrowResourceNotFound() {
        // Arrange
        String colisId = "colis-inexistant";
        when(colisRepository.findById(colisId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            colisService.updateColis(colisId, new ColisRequestDTO());
        });
        verify(colisRepository, never()).save(any());
    }

    // --- Tests pour deleteColis ---

    @Test
    void testDeleteColis_ShouldDeleteMissionsAndColis() {
        // Arrange
        String colisId = "colis-uuid-789";
        when(colisRepository.existsById(colisId)).thenReturn(true);
        // Simuler les void methods
        doNothing().when(missionRepository).deleteAllByColisId(colisId);
        doNothing().when(colisRepository).deleteById(colisId);

        // Act
        colisService.deleteColis(colisId);

        // Assert
        verify(colisRepository, times(1)).existsById(colisId);
        verify(missionRepository, times(1)).deleteAllByColisId(colisId);
        verify(colisRepository, times(1)).deleteById(colisId);
    }

    @Test
    void testDeleteColis_ShouldThrowResourceNotFound() {
        // Arrange
        String colisId = "colis-inexistant";
        when(colisRepository.existsById(colisId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            colisService.deleteColis(colisId);
        });

        // S'assurer qu'aucune suppression n'a été tentée
        verify(missionRepository, never()).deleteAllByColisId(anyString());
        verify(colisRepository, never()).deleteById(anyString());
    }
}