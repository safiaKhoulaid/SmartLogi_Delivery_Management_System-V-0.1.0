package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.mapper.UserMapper;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import com.smartlogi.sdms.domain.repository.ClientExpediteurRepository;
import com.smartlogi.sdms.domain.repository.DestinataireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseUserServiceTest {

    // Mocks pour les dépendances
    @Mock
    private DestinataireRepository destinataireRepository;
    @Mock
    private ClientExpediteurRepository clientExpediteurRepository;
    @Mock
    private UserMapper userMapper;

    // Le service à tester
    @InjectMocks
    private BaseUserService baseUserService;

    // Données de test
    private DestinataireRequestDTO requestDTO;
    private Destinataire destinataire;
    private ClientExpediteur expediteur;
    private String expediteurId;

    @BeforeEach
    void setUp() {
        expediteurId = "exp-123";

        Adresse adresse = new Adresse("1", "Rue Test", "TestVille", "10000", "MAROC", 33.0, -7.0);
        Telephone telephone = new Telephone("+212", "600000001");

        // DTO en entrée
        requestDTO = new DestinataireRequestDTO();
        requestDTO.setNom("Doe");
        requestDTO.setPrenom("John");
        requestDTO.setEmail("john@doe.com");
        requestDTO.setAdresse(adresse);
        requestDTO.setTelephone(telephone);

        // Expéditeur qui crée le destinataire
        expediteur = ClientExpediteur.builder()
                .id(expediteurId)
                .codeClient("CLIENT001")
                .build();

        // Entité destinataire (résultat attendu du mapper)
        destinataire = Destinataire.builder()
                .id("dest-456")
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .adresse(adresse)
                .telephone(telephone)
                .build();
    }

    // --- Test pour createDestinataire ---

    @Test
    @DisplayName("createDestinataire devrait mapper, lier l'expéditeur et sauvegarder")
    void createDestinataire_ShouldMapAndSave() {
        // Arrange
        // 1. Simuler le mappage (DTO -> Entité)
        when(userMapper.toDestinataireEntity(any(DestinataireRequestDTO.class))).thenReturn(destinataire);

        // 2. Simuler la sauvegarde (le repo retourne l'entité sauvegardée)
        // Nous utilisons 'any(Destinataire.class)' car l'objet 'destinataire' sera modifié
        // par l'ajout de l'expéditeur avant la sauvegarde.
        when(destinataireRepository.save(any(Destinataire.class))).thenAnswer(invocation -> {
            // Renvoie l'objet qui a été passé à save()
            return invocation.getArgument(0);
        });

        // Act
        Destinataire result = baseUserService.createDestinataire(requestDTO, expediteur);

        // Assert
        assertNotNull(result);

        // Vérification critique : le service a-t-il correctement lié l'expéditeur ?
        assertNotNull(result.getClientExpediteur());
        assertEquals(expediteurId, result.getClientExpediteur().getId());

        // Vérifier que les données du DTO sont bien présentes
        assertEquals("John", result.getFirstName());
        assertEquals("john@doe.com", result.getEmail());

        // Vérifier les appels mock
        verify(userMapper, times(1)).toDestinataireEntity(requestDTO);
        verify(destinataireRepository, times(1)).save(result);
    }

    // --- Tests pour findClientExpediteurById ---

    @Test
    @DisplayName("findClientExpediteurById devrait retourner l'expéditeur s'il est trouvé")
    void findClientExpediteurById_ShouldReturnExpediteur_WhenFound() {
        // Arrange
        when(clientExpediteurRepository.findById(expediteurId)).thenReturn(Optional.of(expediteur));

        // Act
        Optional<ClientExpediteur> result = baseUserService.findClientExpediteurById(expediteurId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expediteurId, result.get().getId());
        verify(clientExpediteurRepository, times(1)).findById(expediteurId);
    }

    @Test
    @DisplayName("findClientExpediteurById devrait retourner Optional vide s'il n'est pas trouvé")
    void findClientExpediteurById_ShouldReturnEmpty_WhenNotFound() {
        // Arrange
        String badId = "id-inexistant";
        when(clientExpediteurRepository.findById(badId)).thenReturn(Optional.empty());

        // Act
        Optional<ClientExpediteur> result = baseUserService.findClientExpediteurById(badId);

        // Assert
        assertFalse(result.isPresent());
        verify(clientExpediteurRepository, times(1)).findById(badId);
    }
}