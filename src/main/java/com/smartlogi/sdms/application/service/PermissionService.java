package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.domain.exception.PermissionAlreadyExistsException;
import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.Permission;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.repository.PermissionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    // --- 1. CRUD Permissions  ---
    public Permission createPermission(String name) {
        if (permissionRepository.existsByName(name)) {
            throw new PermissionAlreadyExistsException("Permission '" + name + "' existe déjà.");
        }

        Permission p = Permission
                .builder()
                .name(name.toUpperCase())
                .build();

        return permissionRepository.save(p);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public void deletePermission(String id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission introuvable avec ID: " + id);
        }
        permissionRepository.deleteById(id);
    }

    // --- 2. GESTION DES RÔLES (Assignation) ---

    public void assignPermissionToRole(String roleName, String permissionId) {
        // A. Vérifier Role (Enum)
        validateRole(roleName);

        // B. Vérifier Permission
        if (!permissionRepository.existsById(permissionId)) {
            throw new ResourceNotFoundException("Permission introuvable avec ID: " + permissionId);
        }

        // C. Vérifier si déjà assigné (Optionnel, mais clean)
        // (Hna gha n-twkklo 3la DB wla Native Query 'INSERT IGNORE' ila bghiti, 
        // walakin Spring Data JPA @Modifying insert 3adi)
        try {
            permissionRepository.addPermissionToRole(roleName, permissionId);
        } catch (Exception e) {
            log.warn("Permission déjà assignée ou erreur SQL: {}", e.getMessage());
            // T9dri t-ignoré l'erreur ila kant "Duplicate key"
        }
    }

    public void unassignPermissionFromRole(String roleName, String permissionId) {
        validateRole(roleName);
        permissionRepository.removePermissionFromRole(roleName, permissionId);
    }

    public List<Permission> getPermissionsByRole(String roleName) {
        validateRole(roleName);
        return permissionRepository.findAllByRoleName(roleName);
    }

    // Méthode helper bach n-vérifiw wach Role kayn f Enum
    private void validateRole(String roleName) {
        try {
            Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Rôle introuvable: " + roleName);
        }
    }
}