package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.Permission;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.repository.PermissionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    // 1. CREATE
    public Permission createPermission(String name) {
        if (permissionRepository.existsByName(name)) {
            throw new IllegalArgumentException("Permission avec le nom '" + name + "' existe déjà.");
        }
        Permission permission = new Permission();
        permission.setName(name.toUpperCase()); // Dima majuscule (Convention)
        return permissionRepository.save(permission);
    }

    // 2. READ (All)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    // 3. UPDATE
    public Permission updatePermission(String id, String newName) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée avec l'ID: " + id));

        // Ila tbddlat smya, vérifie wach jdida déjà kayna
        if (!permission.getName().equals(newName) && permissionRepository.existsByName(newName)) {
            throw new IllegalArgumentException("Une autre permission porte déjà le nom '" + newName + "'.");
        }

        permission.setName(newName.toUpperCase());
        return permissionRepository.save(permission);
    }

    // 4. DELETE
    public void deletePermission(String id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission non trouvée avec l'ID: " + id);
        }
        // Note: Ila kant l-permission mliaza m3a chi role f 'role_permissions',
        // khassk t-ms7i l-liaison 9bel (ola tkon dayra Cascade delete f DB).
        // Bima ana drna native query f repo, JPA ma 3arfch l-liaison, donc DB li tkllf (Foreign Key).
        permissionRepository.deleteById(id);
    }


    // ... imports

    @Transactional // Mohim bzaf l @Modifying
    public void assignPermissionToRole(String roleName, String permissionId) {
        // 1. Vérifier wach permission kayna
        if (!permissionRepository.existsById(permissionId)) {
            throw new ResourceNotFoundException("Permission introuvable avec ID: " + permissionId);
        }

        // 2. Vérifier wach Role kayn (f Enum)
        try {
            Role.valueOf(roleName); // Ila ma kanch ghadi y-lo7 IllegalArgumentException
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Role introuvable: " + roleName);
        }

        // 3. Exécuter l'ajout
        // (T9dri t-zidi try-catch hna 3la wed 'Duplicate Key' ila kant deja kayna)
        permissionRepository.addPermissionToRole(roleName, permissionId);
    }

    @Transactional
    public void unassignPermissionFromRole(String roleName, String permissionId) {
        permissionRepository.removePermissionFromRole(roleName, permissionId);
    }
}