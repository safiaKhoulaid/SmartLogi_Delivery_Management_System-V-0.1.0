package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.users.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
}
