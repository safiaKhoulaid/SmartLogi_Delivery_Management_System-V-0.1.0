package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.mission.MissionResponseDTO;
import com.smartlogi.sdms.domain.model.entity.Mission;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@SpringBootTest
class MissionMapperTest {

    @Autowired // Correction appliquée
    private MissionMapper missionMapper;

    @Test
    void testToResponseDto_ShouldMapLivreurNomComplet() {
        // Arrange
        Livreur livreur = Livreur.builder()
                .id("liv-1")
                .firstName("Fatima")
                .lastName("Zahra")
                .build();

        Mission mission = Mission.builder()
                .id("mission-1")
                .livreur(livreur)
                .build();

        // Act
        MissionResponseDTO dto = missionMapper.toResponseDto(mission);

        // Assert
        assertNotNull(dto);
        // Test de la logique personnalisée @Named
        assertEquals("Fatima Zahra", dto.getLivreurNomComplet());
    }
}