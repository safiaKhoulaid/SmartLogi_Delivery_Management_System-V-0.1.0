package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.tournee.TourneeRequestDTO;
import com.smartlogi.sdms.application.dto.tournee.TourneeResponseDTO;
import com.smartlogi.sdms.application.service.TourneeService;
import com.smartlogi.sdms.domain.model.enums.StatutTournee;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tournees")
@RequiredArgsConstructor
public class TourneeController {

    private final TourneeService tourneeService;

    /**
     * Crée et optimise une nouvelle tournée.
     */
    @PostMapping("/optimize")
    public ResponseEntity<TourneeResponseDTO> createAndOptimizeTournee(
            @Valid @RequestBody TourneeRequestDTO requestDTO) {
        TourneeResponseDTO response = tourneeService.createAndOptimizeTournee(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Récupère une tournée par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TourneeResponseDTO> getTourneeById(@PathVariable Long id) {
        TourneeResponseDTO response = tourneeService.getTourneeById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère toutes les tournées pour un livreur spécifique.
     */
    @GetMapping("/livreur/{livreurId}")
    public ResponseEntity<List<TourneeResponseDTO>> getTourneesByLivreur(@PathVariable String livreurId) {
        List<TourneeResponseDTO> response = tourneeService.getTourneesByLivreur(livreurId);
        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour le statut d'une tournée (ex: PLANIFIEE -> EN_COURS).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TourneeResponseDTO> updateTourneeStatus(
            @PathVariable Long id,
            @RequestParam StatutTournee statut) {
        TourneeResponseDTO response = tourneeService.updateTourneeStatus(id, statut);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime une tournée et la dé-associe des colis.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournee(@PathVariable Long id) {
        tourneeService.deleteTournee(id);
        return ResponseEntity.noContent().build();
    }
}