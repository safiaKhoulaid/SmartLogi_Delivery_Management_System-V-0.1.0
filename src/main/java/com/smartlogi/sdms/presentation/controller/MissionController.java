package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.mission.MissionRequestDTO;
import com.smartlogi.sdms.application.dto.mission.MissionResponseDTO;
import com.smartlogi.sdms.application.service.MissionService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(MISSION_CREATE)")
    public ResponseEntity<MissionResponseDTO> createMission(
            @Valid @RequestBody MissionRequestDTO requestDTO) throws MessagingException {

        // 1. Déléguer la logique métier (validation, chargement des entités, sauvegarde) au Service
        MissionResponseDTO newMission = missionService.createMission(requestDTO);

        // 2. Retourner le DTO de réponse enveloppé dans le wrapper custom
        return ResponseEntity.status(HttpStatus.CREATED).body(newMission);

    }


}