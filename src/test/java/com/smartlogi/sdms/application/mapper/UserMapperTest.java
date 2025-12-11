package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.user.DestinataireRequestDTO;
import com.smartlogi.sdms.application.dto.user.DestinataireResponseDTO;
import com.smartlogi.sdms.application.dto.user.UserRequestRegisterDTO;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testToClientExpediteur() {
        // Arrange
        UserRequestRegisterDTO dto = new UserRequestRegisterDTO();
        dto.setPrenom("Test");
        dto.setNom("Client");
        dto.setRole(Role.USER);

        // Act
        ClientExpediteur client = userMapper.toClientExpediteur(dto);

        // Assert
        assertNotNull(client);
        assertEquals("Test", client.getFirstName());
        assertEquals("Client", client.getLastName());
        assertNull(client.getId()); // Ignoré
        assertNull(client.getCodeClient()); // Ignoré
    }

    @Test
    void testToDestinataireEntity() {
        // Arrange
        DestinataireRequestDTO dto = new DestinataireRequestDTO();
        dto.setPrenom("Test");
        dto.setNom("Destinataire");
        Telephone tel = new Telephone(null, "611223344");
        dto.setTelephone(tel);

        // Act
        Destinataire destinataire = userMapper.toDestinataireEntity(dto);

        // Assert
        assertNotNull(destinataire);
        assertEquals("Test", destinataire.getFirstName());
        assertEquals("Destinataire", destinataire.getLastName());
        assertEquals(tel, destinataire.getTelephone());
        assertEquals(Role.USER, destinataire.getRole()); // Mappé par défaut
        assertNull(destinataire.getClientExpediteur()); // Ignoré
    }

    @Test
    void testToResponseDTO() {
        // Arrange
        Destinataire destinataire = Destinataire.builder()
                .firstName("Test")
                .lastName("Destinataire")
                .email("test@email.com")
                .build();

        // Act
        DestinataireResponseDTO dto = userMapper.toResponseDTO(destinataire);

        // Assert
        assertNotNull(dto);
        assertEquals("Test", dto.getPrenom());
        assertEquals("Destinataire", dto.getNom());
        assertEquals("test@email.com", dto.getEmail());
    }
}