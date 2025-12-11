package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.livreur.LivreurResponseDTO;
import com.smartlogi.sdms.application.mapper.LivreurMapper;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.StatusLivreur;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import com.smartlogi.sdms.domain.repository.LivreurRepository;
import com.smartlogi.sdms.domain.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivreurServiceTest {

    // Mocks pour les dépendances
    @Mock
    private LivreurRepository livreurRepository;
    @Mock
    private ZoneRepository zoneRepository;
    @Mock
    private LivreurMapper livreurMapper;

    // Le service à tester
    @InjectMocks
    private LivreurService livreurService;

    // Données de test
    private String zoneId;
    private Livreur livreur;
    private LivreurResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        zoneId = "zone-123";

        livreur = Livreur.builder()
                .id("livreur-abc")
                .firstName("Karim")
                .lastName("Alaoui")
                .email("karim@smartlogi.com")
                .statusLivreur(StatusLivreur.DISPONIBLE)
                .telephone(new Telephone("+212", "600000002"))
                .build();

        responseDTO = LivreurResponseDTO.builder()
                .id("livreur-abc")
                .nomComplet("Karim Alaoui")
                .email("karim@smartlogi.com")
                .telephone(new Telephone("+212", "600000002"))
                .build();
    }

    // --- Tests pour findAllDisponibleLivreurByZoneAssigneeId ---

    @Test
    @DisplayName("Devrait retourner les livreurs disponibles si la zone existe")
    void findAllDisponible_ShouldReturnLivreurs_WhenZoneExists() {
        // Arrange
        List<Livreur> livreurs = List.of(livreur);
        List<LivreurResponseDTO> dtos = List.of(responseDTO);

        // 1. Simuler la vérification de la zone (existe)
        when(zoneRepository.existsById(zoneId)).thenReturn(true);

        // 2. Simuler la recherche dans le repository
        when(livreurRepository.findAllByZoneAssigneeIdAndStatusLivreur(zoneId, StatusLivreur.DISPONIBLE))
                .thenReturn(livreurs);

        // 3. Simuler le mappage
        when(livreurMapper.toResponseDTO(livreurs)).thenReturn(dtos);

        // Act
        ResponseEntity<List<LivreurResponseDTO>> response = livreurService.findAllDisponibleLivreurByZoneAssigneeId(zoneId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("livreur-abc", response.getBody().get(0).getId());

        // Vérifier les appels
        verify(zoneRepository, times(1)).existsById(zoneId);
        verify(livreurRepository, times(1)).findAllByZoneAssigneeIdAndStatusLivreur(zoneId, StatusLivreur.DISPONIBLE);
        verify(livreurMapper, times(1)).toResponseDTO(livreurs);
    }

    @Test
    @DisplayName("Devrait lever ResourceNotFoundException si la zone n'existe pas")
    void findAllDisponible_ShouldThrowResourceNotFound_WhenZoneDoesNotExist() {
        // Arrange
        // 1. Simuler la vérification de la zone (n'existe pas)
        when(zoneRepository.existsById(zoneId)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            livreurService.findAllDisponibleLivreurByZoneAssigneeId(zoneId);
        });

        assertTrue(exception.getMessage().contains("La zone logistique avec l'ID '" + zoneId + "' est introuvable."));

        // S'assurer qu'on n'a jamais appelé les autres dépendances
        verify(livreurRepository, never()).findAllByZoneAssigneeIdAndStatusLivreur(any(), any());
        verify(livreurMapper, never()).toResponseDTO(anyList());
    }

    @Test
    @DisplayName("Devrait retourner une liste vide si la zone existe mais aucun livreur n'est disponible")
    void findAllDisponible_ShouldReturnEmptyList_WhenNoLivreursAvailable() {
        // Arrange
        // 1. Simuler la vérification de la zone (existe)
        when(zoneRepository.existsById(zoneId)).thenReturn(true);

        // 2. Simuler la recherche (liste vide)
        when(livreurRepository.findAllByZoneAssigneeIdAndStatusLivreur(zoneId, StatusLivreur.DISPONIBLE))
                .thenReturn(Collections.emptyList());

        // 3. Simuler le mappage (liste vide)
        when(livreurMapper.toResponseDTO(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<LivreurResponseDTO>> response = livreurService.findAllDisponibleLivreurByZoneAssigneeId(zoneId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        // Vérifier les appels
        verify(zoneRepository, times(1)).existsById(zoneId);
        verify(livreurRepository, times(1)).findAllByZoneAssigneeIdAndStatusLivreur(zoneId, StatusLivreur.DISPONIBLE);
        verify(livreurMapper, times(1)).toResponseDTO(Collections.emptyList());
    }
}