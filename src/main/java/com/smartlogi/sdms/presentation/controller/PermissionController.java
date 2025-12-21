package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.dto.permission.AssignPermissionRequestDTO;
import com.smartlogi.sdms.application.dto.permission.PermissionRequestDTO;
import com.smartlogi.sdms.application.dto.permission.PermissionResponseDTO;
import com.smartlogi.sdms.application.dto.permission.RolePermissionsResponseDTO;
import com.smartlogi.sdms.application.mapper.PermissionMapper;
import com.smartlogi.sdms.application.service.PermissionService;
import com.smartlogi.sdms.domain.model.entity.Permission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor

@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    // =================================================
    // 1. CRUD Permissions (Create / List / Delete)
    // =================================================

    @PostMapping
    public ResponseEntity<PermissionResponseDTO> createPermission(@Valid @RequestBody PermissionRequestDTO request) {
        Permission permission = permissionService.createPermission(request.getName());
        PermissionResponseDTO dto = permissionMapper.toResponseDTO(permission);
        dto.setMessage("La permission cree par succès");
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponseDTO>> getAllPermissions() {
        AtomicInteger n = new AtomicInteger(1);

        List<PermissionResponseDTO> dtos = permissionService.getAllPermissions()
                .stream()
                .map(p -> {
                    PermissionResponseDTO dto = permissionMapper.toResponseDTO(p);
                    dto.setMessage("la permission numero " + n.getAndIncrement());
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePermission(@PathVariable String id) {
        permissionService.deletePermission(id);
        String message = " la permission avec lid " + id + "est bien supprime";
        return ResponseEntity.ok(message);
    }

    // =================================================
    // 2. Assignation Rôle <-> Permission
    // =================================================


    @PostMapping("/assign")
    public ResponseEntity<String> assignPermissionToRole(@Valid @RequestBody AssignPermissionRequestDTO request) {
        permissionService.assignPermissionToRole(request.getRole(), request.getPermissionId());
        return ResponseEntity.ok("Permission assignée avec succès au rôle " + request.getRole());
    }


    @PostMapping("/revoke")
    public ResponseEntity<String> revokePermissionFromRole(@Valid @RequestBody AssignPermissionRequestDTO request) {
        permissionService.unassignPermissionFromRole(request.getRole(), request.getPermissionId());
        return ResponseEntity.ok("Permission retirée avec succès du rôle " + request.getRole());
    }


    @GetMapping("/roles/{role}")
    public ResponseEntity<RolePermissionsResponseDTO> getPermissionsByRole(@PathVariable String role) {
        AtomicInteger n = new AtomicInteger(1);

        List<PermissionResponseDTO> permissions = permissionService.getPermissionsByRole(role)
                .stream()
                .map(p -> {
                    PermissionResponseDTO dto = permissionMapper.toResponseDTO(p);
                    dto.setMessage("permission " + n.getAndIncrement());
                    return dto;
                }).toList();

        RolePermissionsResponseDTO response = RolePermissionsResponseDTO.builder()
                .role(role)
                .permissions(permissions)
                .build();

        return ResponseEntity.ok(response);
    }
}