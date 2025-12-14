package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    @Query(value = """
        SELECT p.* FROM permission p 
        JOIN role_permissions rp ON p.id = rp.permission_id 
        WHERE rp.role_name = :roleName
        """, nativeQuery = true)
    List<Permission> findAllByRoleName(@Param("roleName") String roleName);
}