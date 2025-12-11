package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.user.DestinataireResponseDTO;
import com.smartlogi.sdms.application.service.DestinataireService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/destinataires") // Correction : suppression du '/' final
@RequiredArgsConstructor
public class DestinataireController {

    private final DestinataireService destinataireService;

    /**
     * Récupère la liste paginée des destinataires créés par un client expéditeur spécifique.
     *
     * @param clientId L'ID du ClientExpediteur (provenant de l'URL)
     * @param pageable Paramètres de pagination (ex: ?page=0&size=10&sort=firstName,asc)
     * @return Une ResponseEntity 200 OK contenant la page des destinataires.
     */
    @GetMapping("/clientExpediteur/{id}")
    public ResponseEntity<Page<DestinataireResponseDTO>> findAllDestinatairesByClientExpediteurId(
            @PathVariable("id") String clientId,
            @PageableDefault(size = 10, sort = "firstName", direction = Sort.Direction.ASC) Pageable pageable) {

        // 1. Appeler le service pour récupérer la page
        Page<DestinataireResponseDTO> page = destinataireService.getDestinatairesByClient(clientId, pageable);

        // 2. Retourner la page dans une réponse 200 OK
        return ResponseEntity.ok(page);
    }
}