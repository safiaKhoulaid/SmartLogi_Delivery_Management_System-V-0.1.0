package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.colis.ColisRequestDTO;
import com.smartlogi.sdms.application.dto.colis.ColisResponseDTO;
import com.smartlogi.sdms.application.mapper.ColisMapper;
import com.smartlogi.sdms.application.service.ColisService;
import com.smartlogi.sdms.domain.model.entity.Colis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/colis")
@RequiredArgsConstructor // Pour l'injection via constructeur
public class ColisController {

    private final ColisService colisService;
    private final ColisMapper colisMapper; // Assurez-vous d'avoir un bean ColisMapper

    @PostMapping("/create")
    public ResponseEntity<ColisResponseDTO> createColis(@RequestBody ColisRequestDTO colisRequestDTO) { // AJOUT de @RequestBody

        // 1. Appel au service : Création du colis dans la couche métier/domaine
        Colis colis = colisService.createColis(colisRequestDTO);

        // 2. Mappage : Conversion de l'entité Colis vers le DTO de réponse
        ColisResponseDTO responseDTO = colisMapper.toColisResponseDTO(colis);

        // 3. Retourne la réponse avec le statut HTTP 201 (Created)
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }


    @GetMapping("/client/{idClient}")
    @PreAuthorize("authentication.principal.id == #idClient or hasAuthority('GESTIONNAIRE')")
    public ResponseEntity<Page<ColisResponseDTO>> getAllColisByClientExpediteurId(
            @PathVariable("idClient") String idClient,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ColisResponseDTO> colisPage = colisService.getColisByClientExpediteurId(idClient, pageable);
        return ResponseEntity.ok(colisPage);
    }

}