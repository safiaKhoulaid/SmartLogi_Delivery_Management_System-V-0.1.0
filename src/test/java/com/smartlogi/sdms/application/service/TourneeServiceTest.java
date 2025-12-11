package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.routes.OptimizedRouteResponse;
import com.smartlogi.sdms.application.dto.routes.RouteRequest;
import com.smartlogi.sdms.application.dto.tournee.TourneeRequestDTO;
import com.smartlogi.sdms.application.dto.tournee.TourneeResponseDTO;
import com.smartlogi.sdms.application.mapper.TourneeMapper;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.Tournee;
import com.smartlogi.sdms.domain.model.entity.Zone;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.StatutTournee;
import com.smartlogi.sdms.domain.model.enums.TypeVehicule;
import com.smartlogi.sdms.domain.model.enums.UnitePoids;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Poids;
import com.smartlogi.sdms.domain.model.vo.Vehicule;
import com.smartlogi.sdms.domain.repository.ColisRepository;
import com.smartlogi.sdms.domain.repository.LivreurRepository;
import com.smartlogi.sdms.domain.repository.TourneeRepository;
import com.smartlogi.sdms.domain.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourneeServiceTest {

    // Mocks pour les 8 dépendances
    @Mock
    private TourneeRepository tourneeRepository;
    @Mock
    private ColisRepository colisRepository;
    @Mock
    private LivreurRepository livreurRepository;
    @Mock
    private ZoneRepository zoneRepository;
    @Mock
    private RouteOptimizationService routeOptimizationService;
    @Mock
    private TourneeMapper tourneeMapper;

    // Le service à tester
    @InjectMocks
    private TourneeService tourneeService;

    // Données de test
    private TourneeRequestDTO requestDTO;
    private Livreur livreur;
    private Zone zone;
    private Colis colis1;
    private OptimizedRouteResponse optimizedResponse;
    private Tournee savedTournee;
    private TourneeResponseDTO responseDTO;
    private String livreurId = "livreur-123";
    private String zoneId = "zone-abc";
    private String colisId1 = "colis-001";
    private Long tourneeId = 1L;

    @BeforeEach
    void setUp() {
        // --- DTO en entrée ---
        requestDTO = new TourneeRequestDTO();
        requestDTO.setDateTournee(LocalDate.now());
        requestDTO.setLivreurId(livreurId);
        requestDTO.setZoneId(zoneId);
        requestDTO.setColisIds(List.of(colisId1));
        requestDTO.setAlgorithme("NearestNeighbor");

        // --- Dépendances (Entités) ---
        Adresse depotAdresse = new Adresse("DEP", "Rue Depot", "CASABLANCA", "20100", "MAROC", 33.5, -7.5);
        Adresse destAdresse = new Adresse("123", "Rue Dest", "CASABLANCA", "20200", "MAROC", 33.6, -7.6);
        Vehicule vehicule = new Vehicule(TypeVehicule.MOTO, "MOTO-123", 50.0);

        livreur = Livreur.builder().id(livreurId).vehicule(vehicule).build();
        zone = Zone.builder().id(zoneId).nom("Zone A").adresseDepot(depotAdresse).build();

        Destinataire destinataire = Destinataire.builder().id("dest-1").adresse(destAdresse).build();
        colis1 = Colis.builder()
                .id(colisId1)
                .destinataire(destinataire)
                .poids(new Poids(BigDecimal.TEN, UnitePoids.KG))
                .build();

        // --- Simulation de la réponse d'optimisation ---
        OptimizedRouteResponse.RouteDetails routeDetails = OptimizedRouteResponse.RouteDetails.builder()
                .livreurId(livreurId)
                .stopOrder(List.of(colisId1)) // Ordre optimisé
                .routeDistanceKm(10.5)
                .routeEstimatedTimeHours(1.2)
                .build();
        optimizedResponse = OptimizedRouteResponse.builder()
                .tournees(List.of(routeDetails)) // Champ 'tournees'
                .build();

        // --- Entité Tournee (après sauvegarde initiale) ---
        savedTournee = Tournee.builder()
                .id(tourneeId)
                .livreur(livreur)
                .zone(zone)
                .statut(StatutTournee.PLANIFIEE)
                .build();

        // --- DTO de Réponse (final) ---
        responseDTO = TourneeResponseDTO.builder()
                .id(tourneeId)
                .livreurId(livreurId)
                .build();
    }

    // --- Tests pour createAndOptimizeTournee ---

    @Test
    @DisplayName("createAndOptimizeTournee devrait réussir si toutes les données sont valides")
    void createAndOptimizeTournee_ShouldSucceed_WhenDataIsValid() {
        // Arrange
        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(colisRepository.findAllById(List.of(colisId1))).thenReturn(List.of(colis1));

        when(routeOptimizationService.optimizeRoutes(any(RouteRequest.class), eq("NearestNeighbor")))
                .thenReturn(optimizedResponse);

        // Simule la sauvegarde initiale (avant mise à jour des colis)
        when(tourneeRepository.save(any(Tournee.class))).thenReturn(savedTournee);

        // Simule la recherche finale (après mise à jour des colis)
        when(tourneeRepository.findById(tourneeId)).thenReturn(Optional.of(savedTournee));

        when(tourneeMapper.toResponseDTO(savedTournee)).thenReturn(responseDTO);

        // Act
        TourneeResponseDTO result = tourneeService.createAndOptimizeTournee(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(tourneeId, result.getId());

        // Vérification que les colis ont été mis à jour (ordre et FK)
        ArgumentCaptor<Colis> colisCaptor = ArgumentCaptor.forClass(Colis.class);
        verify(colisRepository, times(1)).save(colisCaptor.capture());
        assertEquals(savedTournee, colisCaptor.getValue().getTournee());
        assertEquals(1, colisCaptor.getValue().getOrdreLivraison());

        // Vérification des appels
        verify(livreurRepository, times(1)).findById(livreurId);
        verify(zoneRepository, times(1)).findById(zoneId);
        verify(colisRepository, times(1)).findAllById(anyList());
        verify(routeOptimizationService, times(1)).optimizeRoutes(any(), anyString());
        verify(tourneeRepository, times(1)).save(any(Tournee.class)); // Sauvegarde initiale
        verify(tourneeRepository, times(1)).findById(tourneeId); // Recherche finale
    }

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever ResourceNotFound (Livreur) (Couvre lambda 0)")
    void createAndOptimizeTournee_ShouldThrow_WhenLivreurNotFound() {
        // Arrange
        when(livreurRepository.findById(livreurId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
        verify(tourneeRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever ResourceNotFound (Zone) (Couvre lambda 1)")
    void createAndOptimizeTournee_ShouldThrow_WhenZoneNotFound() {
        // Arrange
        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
    }

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever ResourceNotFound (Colis Mismatch)")
    void createAndOptimizeTournee_ShouldThrow_WhenColisMismatch() {
        // Arrange
        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        // DTO demande 1 colis [colisId1], mais la BDD en renvoie 0
        when(colisRepository.findAllById(List.of(colisId1))).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
    }

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever IllegalState (Zone sans Coords GPS)")
    void createAndOptimizeTournee_ShouldThrow_WhenZoneMissingGps() {
        // Arrange
        Adresse depotSansGps = new Adresse("DEP", "Rue Depot", "CASABLANCA", "20100", "MAROC", null, null);
        Zone zoneSansGps = Zone.builder().id(zoneId).adresseDepot(depotSansGps).build();

        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zoneSansGps));
        when(colisRepository.findAllById(List.of(colisId1))).thenReturn(List.of(colis1));

        // Act & Assert
        // Cette exception vient du 'buildRouteRequest' (méthode privée)
        Exception e = assertThrows(IllegalStateException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
        assertTrue(e.getMessage().contains("n'a pas de coordonnées de dépôt configurées."));
    }

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever IllegalState (Colis sans Coords GPS)")
    void createAndOptimizeTournee_ShouldThrow_WhenColisMissingGps() {
        // Arrange
        Adresse destSansGps = new Adresse("123", "Rue Dest", "CASABLANCA", "20200", "MAROC", null, null);
        Destinataire dest = Destinataire.builder().id("dest-1").adresse(destSansGps).build();
        Colis colisSansGps = Colis.builder()
                .id(colisId1)
                .destinataire(dest)
                .poids(new Poids(BigDecimal.TEN, UnitePoids.KG))
                .build();

        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(colisRepository.findAllById(List.of(colisId1))).thenReturn(List.of(colisSansGps));

        // Act & Assert
        Exception e = assertThrows(IllegalStateException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
        assertTrue(e.getMessage().contains("n'a pas de coordonnées GPS (latitude/longitude) valides."));
    }

    // --- NOUVEAUX TESTS POUR 100% DE COUVERTURE ---

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever IllegalState (Livreur sans Véhicule)")
    void createAndOptimizeTournee_ShouldThrow_WhenLivreurMissingVehicule() {
        // Arrange
        Livreur livreurSansVehicule = Livreur.builder().id(livreurId).vehicule(null).build(); // Vehicule is null

        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreurSansVehicule));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(colisRepository.findAllById(List.of(colisId1))).thenReturn(List.of(colis1));

        // Act & Assert
        Exception e = assertThrows(IllegalStateException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
        assertTrue(e.getMessage().contains("n'a pas de véhicule assigné."));

        verify(routeOptimizationService, never()).optimizeRoutes(any(), anyString());
    }

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever IllegalState (Colis sans Poids)")
    void createAndOptimizeTournee_ShouldThrow_WhenColisMissingPoids() {
        // Arrange
        Colis colisSansPoids = Colis.builder()
                .id(colisId1)
                .destinataire(colis1.getDestinataire())
                .poids(null) // Poids is null
                .build();

        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(colisRepository.findAllById(List.of(colisId1))).thenReturn(List.of(colisSansPoids));

        // Act & Assert
        Exception e = assertThrows(IllegalStateException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
        assertTrue(e.getMessage().contains("n'a pas de poids défini."));
    }

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever IllegalState (Colis sans Destinataire/Adresse)")
    void createAndOptimizeTournee_ShouldThrow_WhenColisMissingDestinataire() {
        // Arrange
        Colis colisSansDestinataire = Colis.builder()
                .id(colisId1)
                .destinataire(null) // Destinataire is null
                .poids(colis1.getPoids())
                .build();

        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(colisRepository.findAllById(List.of(colisId1))).thenReturn(List.of(colisSansDestinataire));

        // Act & Assert
        Exception e = assertThrows(IllegalStateException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
        assertTrue(e.getMessage().contains("n'a pas de destinataire ou d'adresse valide."));
    }

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever IllegalState si l'optimisation retourne une route vide")
    void createAndOptimizeTournee_ShouldThrow_WhenOptimizationReturnsEmpty() {
        // Arrange
        OptimizedRouteResponse emptyResponse = OptimizedRouteResponse.builder().tournees(Collections.emptyList()).build();

        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(colisRepository.findAllById(List.of(colisId1))).thenReturn(List.of(colis1));

        when(routeOptimizationService.optimizeRoutes(any(RouteRequest.class), anyString()))
                .thenReturn(emptyResponse);

        // Act & Assert
        Exception e = assertThrows(IllegalStateException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
        assertTrue(e.getMessage().contains("L'optimisation n'a retourné aucune tournée."));
        verify(tourneeRepository, never()).save(any());
    }

    @Test
    @DisplayName("createAndOptimizeTournee devrait lever ResourceNotFound si la tournée sauvegardée est introuvable (couvre la lambda 2)")
    void createAndOptimizeTournee_ShouldThrowResourceNotFound_OnFinalFindById() {
        // Arrange
        when(livreurRepository.findById(livreurId)).thenReturn(Optional.of(livreur));
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));
        when(colisRepository.findAllById(List.of(colisId1))).thenReturn(List.of(colis1));

        when(routeOptimizationService.optimizeRoutes(any(RouteRequest.class), anyString()))
                .thenReturn(optimizedResponse);

        // 1. Simule la sauvegarde initiale
        when(tourneeRepository.save(any(Tournee.class))).thenReturn(savedTournee);

        // 2. Simule l'échec de la recherche finale (force l'exception)
        when(tourneeRepository.findById(tourneeId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception e = assertThrows(ResourceNotFoundException.class, () -> {
            tourneeService.createAndOptimizeTournee(requestDTO);
        });
        assertTrue(e.getMessage().contains("Erreur lors de la récupération de la tournée sauvegardée."));

        // Vérifie que le colis a été mis à jour (sauvegarde intermédiaire réussie)
        verify(colisRepository, times(1)).save(any(Colis.class));
    }


    @Test
    @DisplayName("getTourneeById devrait lever IllegalArgumentException si l'ID n'est pas trouvé (couvre la lambda 4)")
    void getTourneeById_ShouldThrowIllegalArgumentException_WhenNotFound() {
        // Arrange
        when(tourneeRepository.findByIdWithAssociations(tourneeId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            tourneeService.getTourneeById(tourneeId);
        });
        assertEquals("Tournee introuvable", e.getMessage());
    }

    @Test
    @DisplayName("getTourneeById devrait retourner DTO si trouvée")
    void getTourneeById_ShouldReturnDto_WhenFound() {
        // Arrange
        when(tourneeRepository.findByIdWithAssociations(tourneeId)).thenReturn(Optional.of(savedTournee));
        when(tourneeMapper.toResponseDTO(savedTournee)).thenReturn(responseDTO);

        // Act
        TourneeResponseDTO result = tourneeService.getTourneeById(tourneeId);

        // Assert
        assertNotNull(result);
        assertEquals(tourneeId, result.getId());
        verify(tourneeRepository, times(1)).findByIdWithAssociations(tourneeId);
    }

    @Test
    @DisplayName("getTourneesByLivreur devrait retourner Page DTO")
    void getTourneesByLivreur_ShouldReturnPageDto() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tournee> page = new PageImpl<>(List.of(savedTournee), pageable, 1);
        when(tourneeRepository.findByLivreurIdWithAssociations(eq(livreurId), any(Pageable.class))).thenReturn(page);
        when(tourneeMapper.toResponseDTO(savedTournee)).thenReturn(responseDTO);

        // Act
        Page<TourneeResponseDTO> result = tourneeService.getTourneesByLivreur(livreurId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tourneeRepository, times(1)).findByLivreurIdWithAssociations(eq(livreurId), any(Pageable.class));
    }

    @Test
    @DisplayName("updateTourneeStatus devrait mettre à jour le statut")
    void updateTourneeStatus_ShouldUpdateStatus() {
        // Arrange
        when(tourneeRepository.findById(tourneeId)).thenReturn(Optional.of(savedTournee));
        when(tourneeRepository.save(any(Tournee.class))).thenReturn(savedTournee);
        when(tourneeMapper.toResponseDTO(savedTournee)).thenReturn(responseDTO);

        // Act
        tourneeService.updateTourneeStatus(tourneeId, StatutTournee.EN_COURS);

        // Assert
        // Vérifie que le statut de l'objet 'savedTournee' a été modifié
        assertEquals(StatutTournee.EN_COURS, savedTournee.getStatut());
        verify(tourneeRepository, times(1)).save(savedTournee);
    }

    @Test
    @DisplayName("updateTourneeStatus devrait lever ResourceNotFoundException si l'ID n'est pas trouvé (couvre la lambda 7)")
    void updateTourneeStatus_ShouldThrowResourceNotFoundException_WhenNotFound() {
        // Arrange
        when(tourneeRepository.findById(tourneeId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception e = assertThrows(ResourceNotFoundException.class, () -> {
            tourneeService.updateTourneeStatus(tourneeId, StatutTournee.EN_COURS);
        });
        assertTrue(e.getMessage().contains("Tournée non trouvée avec l'ID: " + tourneeId));
        verify(tourneeRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTournee devrait dé-associer les colis et supprimer la tournée")
    void deleteTournee_ShouldDisassociateColisAndRemove() {
        // Arrange
        // La tournée doit avoir un colis pour tester la dé-association
        savedTournee.setLivraisons(List.of(colis1));
        colis1.setTournee(savedTournee); // Lier le colis

        when(tourneeRepository.findById(tourneeId)).thenReturn(Optional.of(savedTournee));

        // Act
        tourneeService.deleteTournee(tourneeId);

        // Assert
        // 1. Vérifier que le colis a été sauvegardé (mis à jour)
        ArgumentCaptor<Colis> colisCaptor = ArgumentCaptor.forClass(Colis.class);
        verify(colisRepository, times(1)).save(colisCaptor.capture());
        assertNull(colisCaptor.getValue().getTournee()); // Vérifie la dé-association

        // 2. Vérifier que la tournée a été supprimée
        verify(tourneeRepository, times(1)).delete(savedTournee);
    }

    @Test
    @DisplayName("deleteTournee devrait lever ResourceNotFoundException si l'ID n'est pas trouvé (couvre la lambda 5)")
    void deleteTournee_ShouldThrowResourceNotFoundException_WhenNotFound() {
        // Arrange
        when(tourneeRepository.findById(tourneeId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception e = assertThrows(ResourceNotFoundException.class, () -> {
            tourneeService.deleteTournee(tourneeId);
        });
        assertTrue(e.getMessage().contains("Tournée non trouvée avec l'ID: " + tourneeId));
        verify(tourneeRepository, never()).delete(any());
    }
}