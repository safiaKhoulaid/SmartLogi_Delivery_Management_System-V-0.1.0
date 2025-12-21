package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.Email.EmailRequest;
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
import com.smartlogi.sdms.domain.model.enums.UnitePoids;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Poids;
import com.smartlogi.sdms.domain.repository.*;
import jakarta.mail.MessagingException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColisServiceTest {

    @Mock private ColisRepository colisRepository;
    @Mock private DestinataireRepository destinataireRepository;
    @Mock private DestinataireService destinataireService;
    @Mock private ColisMapper colisMapper;
    @Mock private ZoneRepository zoneRepository;
    @Mock private ClientExpediteurRepository clientExpediteurRepository;
    @Mock private MissionRepository missionRepository;
    @Mock private EmailService emailService;
    @Mock private LivreurRepository livreurRepository;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private ColisService colisService;

    // Données de test
    private ClientExpediteur expediteur;
    private Destinataire destinataire;
    private Zone zone;
    private ColisRequestDTO requestDTO;
    private Colis colis;
    private ColisResponseDTO responseDTO; // 👈 Nouveau champ pour le retour
    private Adresse adresse;
    private DestinataireRequestDTO destinataireInfo;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        adresse = new Adresse("123", "Rue Test", "CASABLANCA", "20000", "MAROC", 33.5, -7.6);

        expediteur = new ClientExpediteur();
        expediteur.setId("expediteur-123");
        expediteur.setEmail("client@test.com");
        expediteur.setFirstName("John");
        expediteur.setLastName("Doe");

        destinataire = new Destinataire();
        destinataire.setId("destinataire-456");
        destinataire.setEmail("destinataire@test.com");
        destinataire.setFirstName("Jane");
        destinataire.setAdresse(adresse);

        zone = new Zone();
        zone.setId("zone-abc");
        zone.setCodePostal("20000");

        destinataireInfo = new DestinataireRequestDTO();
        destinataireInfo.setEmail("destinataire@test.com");
        destinataireInfo.setNom("Desti");
        destinataireInfo.setPrenom("Test");
        destinataireInfo.setAdresse(adresse);

        requestDTO = new ColisRequestDTO();
        requestDTO.setDestinataireInfo(destinataireInfo);
        requestDTO.setDescription("Test Colis");
        requestDTO.setPoids(new Poids(BigDecimal.ONE, UnitePoids.KG));

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
                .trackingCode("TRACK-123")
                .build();

        // 👇 Initialisation du DTO attendu en retour
        responseDTO = ColisResponseDTO.builder()
                .id("colis-uuid-789")
                .description("Test Colis")
                .trackingCode("TRACK-123")
                .statut(StatusColis.CREE)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- Tests createColis ---

    @Test
    void testCreateColis_AsClient_ShouldReturnDTO() throws MessagingException {
        // 1. Mock Auth
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("client@test.com");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")))
                .when(authentication).getAuthorities();

        // 2. Mock Logic
        when(clientExpediteurRepository.findByEmail("client@test.com")).thenReturn(Optional.of(expediteur));
        when(destinataireService.findOrCreateDestinataire(any(), any())).thenReturn(destinataire);
        when(zoneRepository.findByVilleAndCodePostal(anyString(), anyString())).thenReturn(Optional.of(zone));
        when(colisMapper.toEntity(any(ColisRequestDTO.class))).thenReturn(colis);
        when(colisRepository.save(any(Colis.class))).thenReturn(colis);

        // 👇 IMPORTANT : Mock du mapping final Entité -> DTO
        when(colisMapper.toColisResponseDTO(any(Colis.class))).thenReturn(responseDTO);

        // 3. Act
        ColisResponseDTO result = colisService.createColis(requestDTO); // Retourne DTO maintenant

        // 4. Assert
        assertNotNull(result);
        assertEquals("colis-uuid-789", result.getId());
        assertEquals("TRACK-123", result.getTrackingCode());

        verify(emailService).sendEmail(any(EmailRequest.class));
        verify(colisMapper).toColisResponseDTO(colis); // Vérifie que le mapper a été appelé
    }

    @Test
    void testCreateColis_AsGestionnaire_ShouldReturnDTO() throws MessagingException {
        // 1. Mock Auth
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@test.com");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GESTIONNAIRE")))
                .when(authentication).getAuthorities();
        requestDTO.setExpediteurId("expediteur-123");

        // 2. Mock Logic
        when(clientExpediteurRepository.findById("expediteur-123")).thenReturn(Optional.of(expediteur));
        when(destinataireService.findOrCreateDestinataire(any(), any())).thenReturn(destinataire);
        when(zoneRepository.findByVilleAndCodePostal(anyString(), anyString())).thenReturn(Optional.of(zone));
        when(colisMapper.toEntity(any())).thenReturn(colis);
        when(colisRepository.save(any())).thenReturn(colis);

        // 👇 IMPORTANT : Mock du mapping final
        when(colisMapper.toColisResponseDTO(any(Colis.class))).thenReturn(responseDTO);

        // 3. Act
        ColisResponseDTO result = colisService.createColis(requestDTO);

        // 4. Assert
        assertNotNull(result);
        assertEquals("colis-uuid-789", result.getId());
    }

    @Test
    void testCreateColis_AsGestionnaire_MissingExpediteurId_ShouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GESTIONNAIRE")))
                .when(authentication).getAuthorities();
        requestDTO.setExpediteurId(null);

        assertThrows(ValidationException.class, () -> colisService.createColis(requestDTO));
    }

    @Test
    void testCreateColis_ZoneNotFound_ShouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();
        when(clientExpediteurRepository.findByEmail(any())).thenReturn(Optional.of(expediteur));
        when(destinataireService.findOrCreateDestinataire(any(), any())).thenReturn(destinataire);

        // Mock Zone NOT FOUND
        when(zoneRepository.findByVilleAndCodePostal(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> colisService.createColis(requestDTO));
    }

    // --- Tests getColisByClientExpediteurId ---

    @Test
    void testGetColisByClientExpediteurId_ShouldReturnPage() {
        String idClient = "expediteur-123";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Colis> colisPage = new PageImpl<>(Collections.singletonList(colis));

        when(clientExpediteurRepository.existsById(idClient)).thenReturn(true);
        when(colisRepository.findAllByClientExpediteurId(idClient, pageable)).thenReturn(colisPage);
        when(colisMapper.toColisResponseDTO(any(Colis.class))).thenReturn(responseDTO);

        Page<ColisResponseDTO> result = colisService.getColisByClientExpediteurId(idClient, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // --- Tests updateColis ---

    @Test
    void testUpdateColis_ShouldUpdateFields() {
        String colisId = "colis-789";
        ColisRequestDTO updateDTO = new ColisRequestDTO();
        updateDTO.setDescription("Nouvelle desc");

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colis));
        when(colisRepository.save(any(Colis.class))).thenReturn(colis);
        when(colisMapper.toColisResponseDTO(any(Colis.class))).thenReturn(responseDTO);

        ColisResponseDTO result = colisService.updateColis(colisId, updateDTO);

        assertNotNull(result);
        // Ici on vérifie juste que ça retourne bien le DTO mocké
        assertEquals(responseDTO.getId(), result.getId());
    }

    // --- Tests deleteColis ---

    @Test
    void testDeleteColis_ShouldDeleteMissionsAndColis() {
        String colisId = "colis-789";
        when(colisRepository.existsById(colisId)).thenReturn(true);

        colisService.deleteColis(colisId);

        verify(missionRepository).deleteAllByColisId(colisId);
        verify(colisRepository).deleteById(colisId);
    }
}