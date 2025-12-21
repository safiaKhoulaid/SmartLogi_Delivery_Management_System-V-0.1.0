package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.application.service.ColisService;
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/colis")
@RequiredArgsConstructor
public class ColisController {

    private final ColisService colisService;
    private final ColisMapper colisMapper;

    // ========================================================================
    // 1. GESTION DES COLIS (CRUD CLIENT & GESTIONNAIRE)
    // ========================================================================

    /**
     * Création d'un nouveau colis.
     * Accès : Clients uniquement (ceux qui ont la permission COLIS_CREATE).
     */
    @PostMapping
    @PreAuthorize("hasAuthority('COLIS_CREATE')")
    public ResponseEntity<ColisResponseDTO> createColis(@RequestBody ColisRequestDTO colisRequestDTO) throws MessagingException {
        log.info("Création d'un colis demandée pour l'expéditeur : {}", colisRequestDTO.getExpediteurId());
        ColisResponseDTO colis = colisService.createColis(colisRequestDTO);
        colis.builder().message("la colis créer par succès").build();
        return new ResponseEntity<>(colis, HttpStatus.CREATED);
    }

    /**
     * Modification d'un colis existant.
     * Accès : Gestionnaires (COLIS_UPDATE).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COLIS_UPDATE')")
    public ResponseEntity<ColisResponseDTO> updateColis(@PathVariable("id") String id, @RequestBody ColisRequestDTO requestDTO) {
        log.info("Mise à jour du colis ID : {}", id);
        ColisResponseDTO response = colisService.updateColis(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Suppression d'un colis.
     * Accès : Admin uniquement (COLIS_DELETE).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COLIS_DELETE')")
    public ResponseEntity<Void> deleteColis(@PathVariable("id") String id) {
        log.warn("Suppression du colis ID : {}", id);
        colisService.deleteColis(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================================================
    // 2. RECHERCHE ET CONSULTATION (READ)
    // ========================================================================

    /**
     * Récupérer les colis d'un client spécifique.
     * Accès : Le Client lui-même ou le Gestionnaire.
     */
    @GetMapping("/client/{idClient}")
    @PreAuthorize("hasAuthority('COLIS_READ')")
    public ResponseEntity<Page<ColisResponseDTO>> getAllColisByClientExpediteurId(
            @PathVariable("idClient") String idClient,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Récupération des colis pour le client : {}", idClient);
        Page<ColisResponseDTO> colisPage = colisService.getColisByClientExpediteurId(idClient, pageable);
        return ResponseEntity.ok(colisPage);
    }

    /**
     * Recherche avancée globale (Filtres, Tri, Pagination).
     * Accès : Admin et Gestionnaire.
     */
    @GetMapping("/search") // J'ai renommé /admin/all en /search c'est plus propre
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<ColisResponseDTO>> searchAllColis(
            @RequestParam(required = false) StatusColis statut,
            @RequestParam(required = false) PriorityColis priorite,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String trackingCode,
            @RequestParam(required = false) String expediteurId,
            @PageableDefault(size = 20, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ColisResponseDTO> result = colisService.searchColisAdmin(
                statut, priorite, ville, description, trackingCode, expediteurId, pageable
        );
        return ResponseEntity.ok(result);
    }

    // ========================================================================
    // 3. ESPACE LIVREUR
    // ========================================================================

    /**
     * Récupérer les colis assignés au livreur connecté.
     * Accès : Livreur uniquement.
     */
    @GetMapping("/mes-colis")
    @PreAuthorize("hasAuthority('MISSION_READ')") // Utilisation de la permission spécifique du Livreur
    public ResponseEntity<List<ColisResponseDTO>> getMesColis() {
        List<ColisResponseDTO> mesColis = colisService.getColisForAuthenticatedLivreur();
        return ResponseEntity.ok(mesColis != null ? mesColis : Collections.emptyList());
    }

    // ========================================================================
    // 4. PUBLIC (SANS AUTHENTIFICATION)
    // ========================================================================

    /**
     * Suivi public d'un colis via son code de tracking.
     * Accès : Public (Pas de @PreAuthorize).
     */
    @GetMapping("/suivi/{trackingCode}")
    public ResponseEntity<ColisResponseDTO> suivreLeColis(@PathVariable String trackingCode) {
        log.info("Suivi public demandé pour le code : {}", trackingCode);
        ColisResponseDTO response = colisService.suivreColis(trackingCode);
        return ResponseEntity.ok(response);
    }
}