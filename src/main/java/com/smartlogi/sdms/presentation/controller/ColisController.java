package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.application.service.ColisService;
import com.smartlogi.sdms.domain.model.entity.Colis;
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

    @PostMapping()
    public ResponseEntity<ColisResponseDTO> createColis(@RequestBody ColisRequestDTO colisRequestDTO) throws MessagingException {
        log.info("Requête POST /api/v1/colis reçue.");
        Colis colis = colisService.createColis(colisRequestDTO);
        ColisResponseDTO responseDTO = colisMapper.toColisResponseDTO(colis);
        log.info("Colis créé avec ID: {}", responseDTO.getId());
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }


    @GetMapping("/client/{idClient}")
//    @PreAuthorize("authentication.principal.id == #idClient or hasAuthority('GESTIONNAIRE')")
    public ResponseEntity<Page<ColisResponseDTO>> getAllColisByClientExpediteurId(@PathVariable("idClient") String idClient, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Requête GET /api/v1/colis/client/{} reçue | Page: {}, Taille: {}", idClient, pageable.getPageNumber(), pageable.getPageSize()); // 👈 AJOUT
        Page<ColisResponseDTO> colisPage = colisService.getColisByClientExpediteurId(idClient, pageable);
        log.info("Retour de {} colis pour le client {}", colisPage.getTotalElements(), idClient); // 👈 AJOUT
        return ResponseEntity.ok(colisPage);
    }

    // --- 👇 AJOUT DES LOGS POUR UPDATE 👇 ---
    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('GESTIONNAIRE')")
    public ResponseEntity<ColisResponseDTO> updateColis(@PathVariable("id") String id, @RequestBody ColisRequestDTO requestDTO) {

        log.info("Requête PUT /api/v1/colis/{} reçue", id); // 👈 AJOUT
        ColisResponseDTO response = colisService.updateColis(id, requestDTO);
        log.info("Colis ID: {} mis à jour.", response.getId()); // 👈 AJOUT
        return ResponseEntity.ok(response);
    }

    // --- 👇 AJOUT DES LOGS POUR DELETE 👇 ---
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('GESTIONNAIRE')")
    public ResponseEntity<Void> deleteColis(@PathVariable("id") String id) {

        log.info("Requête DELETE /api/v1/colis/{} reçue", id); // 👈 AJOUT
        colisService.deleteColis(id);
        log.info("Colis ID: {} supprimé.", id); // 👈 AJOUT
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ColisResponseDTO>> search(@RequestParam(required = false) StatusColis statut, @RequestParam(required = false) PriorityColis priorite, @RequestParam(required = false) String ville, @RequestParam(required = false) String description, @RequestParam(required = false) String expediteurId, @PageableDefault(size = 10, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ColisResponseDTO> result = colisService.searchColisSimple(statut, priorite, ville, description, expediteurId, pageable);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/suivi/{trackingCode}")
    public ResponseEntity<ColisResponseDTO> suivreLeColis(@PathVariable String trackingCode) {
        log.info("Requête de suivi pour le code : {}", trackingCode);
        ColisResponseDTO response = colisService.suivreColis(trackingCode);
        return ResponseEntity.ok(response);
    }

    // ... imports

    // 👇 AJOUTE CET ENDPOINT
    @GetMapping("/mes-colis")
    @PreAuthorize("hasAuthority('LIVREUR') or hasRole('LIVREUR')") // Sécurité renforcée
    public ResponseEntity<List<ColisResponseDTO>> getMesColis() {

        List<ColisResponseDTO> mesColis = colisService.getColisForAuthenticatedLivreur();
        if (mesColis == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(mesColis);
    }
}