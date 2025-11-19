package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
class ColisMapperTest {

    @Autowired // Correction appliquée
    private ColisMapper colisMapper;

    @Test
    void testMapNomComplet() {
        // Arrange
        ClientExpediteur client = ClientExpediteur.builder()
                .firstName("Ali")
                .lastName("Hassani")
                .telephone(new Telephone("+212", "611223344"))
                .build();
        Colis colis = Colis.builder().clientExpediteur(client).build();

        // Act
        ColisResponseDTO dto = colisMapper.toColisResponseDTO(colis);

        // Assert
        assertNotNull(dto);
        // Test de la méthode @Named "mapNomComplet"
        assertEquals("Ali Hassani", dto.getClientExpediteurNom());
        // Test du traducteur de Value Object
        assertEquals("+212611223344", dto.getClientExpediteurTelephone());
    }
}