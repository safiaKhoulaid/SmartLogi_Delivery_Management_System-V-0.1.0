package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.livreur.LivreurResponseDTO;
import com.smartlogi.sdms.application.mapper.LivreurMapper;
import com.smartlogi.sdms.application.service.LivreurService;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // Import requis
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/livreurs")
@RequiredArgsConstructor
public class LivreurController {

    private final LivreurService livreurService;
    private final LivreurMapper livreurMapper;

    @GetMapping("/disponibles")
    public ResponseEntity<List<LivreurResponseDTO>> getAllDisponibleLivreurByZoneId(@RequestParam String zoneId) {

        // 1. Appel au service (le service gère l'erreur 404 si la zone n'existe pas)
       ResponseEntity< List<LivreurResponseDTO>> dtos= livreurService.findAllDisponibleLivreurByZoneAssigneeId(zoneId); // Nom de méthode corrigé

        // 2. Mappage des Entités vers les DTOs

        // 3. Retourner 200 OK avec la liste (sera [] si vide, ce qui est correct)
        return dtos;
    }
}