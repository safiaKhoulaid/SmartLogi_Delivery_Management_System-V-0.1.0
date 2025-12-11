package com.smartlogi.sdms.application.mapper;

import com.smartlogi.sdms.application.dto.tournee.TourneeResponseDTO;
import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.entity.Tournee;
import com.smartlogi.sdms.domain.model.entity.Zone;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TourneeMapperTest {

    @Autowired
    private TourneeMapper tourneeMapper;

    @Test
    void testToResponseDTO() {
        // Arrange
        Zone zone = Zone.builder().id("zone-1").nom("MaZone").build();
        Livreur livreur = Livreur.builder().id("livreur-1").build();
        Colis colis = Colis.builder().id("colis-1").description("Test Colis").build();

        Tournee tournee = Tournee.builder()
                .id(1L)
                .zone(zone)
                .livreur(livreur)
                .livraisons(List.of(colis))
                .build();

        // Act
        TourneeResponseDTO dto = tourneeMapper.toResponseDTO(tournee);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("zone-1", dto.getZoneId());
        assertEquals("MaZone", dto.getNomZone());
        assertEquals("livreur-1", dto.getLivreurId());

        assertNotNull(dto.getLivraisons());
        assertEquals(1, dto.getLivraisons().size());
        assertEquals("colis-1", dto.getLivraisons().get(0).getId());
    }
}