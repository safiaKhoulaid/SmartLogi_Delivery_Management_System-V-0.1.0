package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.application.service.ColisService;
import com.smartlogi.sdms.domain.model.entity.Colis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 👈 AJOUT
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// import java.util.List; // N'est plus nécessaire si vous n'utilisez que Page

@Slf4j // 👈 AJOUT
@RestController
@RequestMapping("/api/v1/colis")
@RequiredArgsConstructor
public class ColisController {

    private final ColisService colisService;
    private final ColisMapper colisMapper;

    @PostMapping("/create")
    public ResponseEntity<ColisResponseDTO> createColis(@RequestBody ColisRequestDTO colisRequestDTO) {
        log.info("Requête POST /api/v1/colis/create reçue."); // 👈 AJOUT
        Colis colis = colisService.createColis(colisRequestDTO);
        ColisResponseDTO responseDTO = colisMapper.toColisResponseDTO(colis);
        log.info("Colis créé avec ID: {}", responseDTO.getId()); // 👈 AJOUT
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }


    @GetMapping("/client/{idClient}")
//    @PreAuthorize("authentication.principal.id == #idClient or hasAuthority('GESTIONNAIRE')")
    public ResponseEntity<Page<ColisResponseDTO>> getAllColisByClientExpediteurId(
            @PathVariable("idClient") String idClient,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Requête GET /api/v1/colis/client/{} reçue | Page: {}, Taille: {}", idClient, pageable.getPageNumber(), pageable.getPageSize()); // 👈 AJOUT
        Page<ColisResponseDTO> colisPage = colisService.getColisByClientExpediteurId(idClient, pageable);
        log.info("Retour de {} colis pour le client {}", colisPage.getTotalElements(), idClient); // 👈 AJOUT
        return ResponseEntity.ok(colisPage);
    }

    // --- 👇 AJOUT DES LOGS POUR UPDATE 👇 ---
    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('GESTIONNAIRE')")
    public ResponseEntity<ColisResponseDTO> updateColis(
            @PathVariable("id") String id,
            @RequestBody ColisRequestDTO requestDTO) {

        log.info("Requête PUT /api/v1/colis/{} reçue", id); // 👈 AJOUT
        ColisResponseDTO response = colisService.updateColis(id, requestDTO);
        log.info("Colis ID: {} mis à jour.", response.getId()); // 👈 AJOUT
        return ResponseEntity.ok(response);
    }

    // --- 👇 AJOUT DES LOGS POUR DELETE 👇 ---
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('GESTIONNAIRE')")
    public ResponseEntity<Void> deleteColis(
            @PathVariable("id") String id) {

        log.info("Requête DELETE /api/v1/colis/{} reçue", id); // 👈 AJOUT
        colisService.deleteColis(id);
        log.info("Colis ID: {} supprimé.", id); // 👈 AJOUT
        return ResponseEntity.noContent().build();
    }
}