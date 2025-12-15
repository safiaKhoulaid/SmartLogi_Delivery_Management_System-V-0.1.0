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

@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
// 🔒 SÉCURITÉ: Ghir ADMIN

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
        return new ResponseEntity<>(permissionMapper.toResponseDTO(permission), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponseDTO>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissionMapper.toResponseDTOs(permissions));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable String id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

    // =================================================
    // 2. Assignation Rôle <-> Permission
    // =================================================

    // Endpoint: POST /assign
    // Body: { "role": "LIVREUR", "permissionId": 1 }
    @PostMapping("/assign")
    public ResponseEntity<String> assignPermissionToRole(@Valid @RequestBody AssignPermissionRequestDTO request) {
        permissionService.assignPermissionToRole(request.getRole(), request.getPermissionId());
        return ResponseEntity.ok("Permission assignée avec succès au rôle " + request.getRole());
    }

    // Endpoint: POST /revoke (wla DELETE /assign)
    // Body: { "role": "LIVREUR", "permissionId": 1 }
    @PostMapping("/revoke")
    public ResponseEntity<String> revokePermissionFromRole(@Valid @RequestBody AssignPermissionRequestDTO request) {
        permissionService.unassignPermissionFromRole(request.getRole(), request.getPermissionId());
        return ResponseEntity.ok("Permission retirée avec succès du rôle " + request.getRole());
    }

    // Endpoint: GET /roles/{role}
    // Return: { "role": "LIVREUR", "permissions": [ ... ] }
    @GetMapping("/roles/{role}")
    public ResponseEntity<RolePermissionsResponseDTO> getPermissionsByRole(@PathVariable String role) {
        List<Permission> permissions = permissionService.getPermissionsByRole(role);

        RolePermissionsResponseDTO response = RolePermissionsResponseDTO.builder()
                .role(role)
                .permissions(permissionMapper.toResponseDTOs(permissions))
                .build();

        return ResponseEntity.ok(response);
    }
}