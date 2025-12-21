package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.GestionnaireUpdateRequest;
import com.smartlogi.sdms.application.dto.auth.RegisterRequest;
import com.smartlogi.sdms.application.service.GestionnaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/gestionnaires")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminGestionnaireController {

    private final GestionnaireService gestionnaireService;

    @PostMapping
    public ResponseEntity<RegisterRequest> create(@RequestBody @Valid RegisterRequest dto) {
        return new ResponseEntity<>(gestionnaireService.createGestionnaire(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<RegisterRequest>> getAll(Pageable pageable) {
        return ResponseEntity.ok(gestionnaireService.getAllGestionnaires(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegisterRequest> getOne(@PathVariable String id) {
        return ResponseEntity.ok(gestionnaireService.getGestionnaireById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegisterRequest> update(@PathVariable String id, @RequestBody @Valid GestionnaireUpdateRequest dto) {
        return ResponseEntity.ok(gestionnaireService.updateGestionnaire(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        gestionnaireService.deleteGestionnaire(id);
        return ResponseEntity.noContent().build();
    }
}