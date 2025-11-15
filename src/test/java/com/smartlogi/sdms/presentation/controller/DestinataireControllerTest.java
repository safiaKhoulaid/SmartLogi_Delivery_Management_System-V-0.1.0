package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.user.DestinataireResponseDTO;
import com.smartlogi.sdms.application.service.DestinataireService;
import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.infrastructure.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DestinataireController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser // Sécurise le contrôleur
class DestinataireControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DestinataireService destinataireService;

    // Mocks requis par Spring Security
    @MockBean
    private JWTService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private String clientId;
    private Page<DestinataireResponseDTO> responsePage;

    @BeforeEach
    void setUp() {
        clientId = "client-123";
        DestinataireResponseDTO dto = DestinataireResponseDTO.builder().id("dest-456").nom("Doe").build();
        responsePage = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
    }

    @Test
    @DisplayName("GET /clientExpediteur/{id} - Succès (200 OK)")
    void findAllDestinataires_ShouldReturn200_WhenFound() throws Exception {
        // Arrange
        when(destinataireService.getDestinatairesByClient(eq(clientId), any(Pageable.class)))
                .thenReturn(responsePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/destinataires/clientExpediteur/{id}", clientId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].id", is("dest-456")));
    }

    @Test
    @DisplayName("GET /clientExpediteur/{id} - Non trouvé (404 NOT FOUND)")
    void findAllDestinataires_ShouldReturn404_WhenClientNotFound() throws Exception {
        // Arrange
        when(destinataireService.getDestinatairesByClient(eq(clientId), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("ClientExpediteur", "id", clientId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/destinataires/clientExpediteur/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("ClientExpediteur non trouvé avec id : 'client-123'")));
    }
}