package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {



    boolean existsByName(String name);
    @Modifying // Darouri hit hada INSERT machi SELECT
    @Query(value = "INSERT INTO role_permissions (role_name, permission_id) VALUES (:roleName, :permissionId)", nativeQuery = true)
    void addPermissionToRole(@Param("roleName") String roleName, @Param("permissionId") String permissionId);

    // 2. Bach n-7iyedou l-liaison (Delete mn table liaison)
    @Modifying
    @Query(value = "DELETE FROM role_permissions WHERE role_name = :roleName AND permission_id = :permissionId", nativeQuery = true)
    void removePermissionFromRole(@Param("roleName") String roleName, @Param("permissionId") String permissionId);

    // 3. (Déjà dernaha) Bach n-jibo permissions dyal role
    @Query(value = "SELECT p.* FROM permission p JOIN role_permissions rp ON p.id = rp.permission_id WHERE rp.role_name = :roleName", nativeQuery = true)
    List<Permission> findAllByRoleName(@Param("roleName") String roleName);
}