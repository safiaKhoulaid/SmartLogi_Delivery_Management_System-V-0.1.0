package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.user.DestinataireResponseDTO;
import com.smartlogi.sdms.application.mapper.UserMapper;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.repository.ClientExpediteurRepository;
import com.smartlogi.sdms.domain.repository.DestinataireRepository;
import com.smartlogi.sdms.domain.model.vo.Adresse; // Import nécessaire
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestinataireServiceTest {

    // Mocks pour les dépendances
    @Mock
    private DestinataireRepository destinataireRepository;
    @Mock
    private ClientExpediteurRepository clientExpediteurRepository;
    @Mock
    private BaseUserService baseUserService;
    @Mock
    private UserMapper userMapper;

    // Le service à tester
    @InjectMocks
    private DestinataireService destinataireService;

    // Données de test
    private String clientId;
    private Pageable pageable;
    private Destinataire destinataire;
    private DestinataireResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        clientId = "client-123";
        pageable = PageRequest.of(0, 10);

        Adresse adresse = new Adresse("1", "Rue Test", "TestVille", "10000", "MAROC", 33.0, -7.0);

        destinataire = Destinataire.builder()
                .id("dest-456")
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .adresse(adresse)
                .build();

        responseDTO = DestinataireResponseDTO.builder()
                .id("dest-456")
                .prenom("John")
                .nom("Doe")
                .email("john@doe.com")
                .adresse(adresse)
                .build();
    }

    // --- Tests pour getDestinatairesByClient ---

    @Test
    @DisplayName("Devrait retourner une page de destinataires si le client existe")
    void getDestinatairesByClient_ShouldReturnPage_WhenClientExists() {
        // Arrange
        // 1. Simuler la vérification d'existence du client
        when(clientExpediteurRepository.existsById(clientId)).thenReturn(true);

        // 2. Simuler la page retournée par le repository
        Page<Destinataire> destinatairePage = new PageImpl<>(List.of(destinataire), pageable, 1);
        when(destinataireRepository.findAllByClientExpediteurId(eq(clientId), any(Pageable.class)))
                .thenReturn(destinatairePage);

        // 3. Simuler le mappage
        when(userMapper.toResponseDTO(any(Destinataire.class))).thenReturn(responseDTO);

        // Act
        Page<DestinataireResponseDTO> result = destinataireService.getDestinatairesByClient(clientId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("dest-456", result.getContent().get(0).getId());

        // Vérifier les appels
        verify(clientExpediteurRepository, times(1)).existsById(clientId);
        verify(destinataireRepository, times(1)).findAllByClientExpediteurId(clientId, pageable);
        verify(userMapper, times(1)).toResponseDTO(destinataire);
    }

    @Test
    @DisplayName("Devrait lever ResourceNotFoundException si le client n'existe pas")
    void getDestinatairesByClient_ShouldThrowResourceNotFound_WhenClientDoesNotExist() {
        // Arrange
        // 1. Simuler la vérification d'existence (échec)
        when(clientExpediteurRepository.existsById(clientId)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            destinataireService.getDestinatairesByClient(clientId, pageable);
        });

        assertEquals("ClientExpediteur non trouvé avec id : 'client-123'", exception.getMessage());

        // S'assurer qu'on n'a jamais appelé le repository de destinataires
        verify(destinataireRepository, never()).findAllByClientExpediteurId(any(), any());
        verify(userMapper, never()).toResponseDTO(any());
    }

    @Test
    @DisplayName("Devrait retourner une page vide si le client existe mais n'a pas de destinataires")
    void getDestinatairesByClient_ShouldReturnEmptyPage_WhenNoDestinatairesFound() {
        // Arrange
        // 1. Simuler la vérification d'existence du client
        when(clientExpediteurRepository.existsById(clientId)).thenReturn(true);

        // 2. Simuler une page vide retournée par le repository
        Page<Destinataire> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(destinataireRepository.findAllByClientExpediteurId(eq(clientId), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<DestinataireResponseDTO> result = destinataireService.getDestinatairesByClient(clientId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        // Vérifier les appels
        verify(clientExpediteurRepository, times(1)).existsById(clientId);
        verify(destinataireRepository, times(1)).findAllByClientExpediteurId(clientId, pageable);
        // Le mapper ne doit jamais être appelé si la liste est vide
        verify(userMapper, never()).toResponseDTO(any());
    }
}