package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.users.Gestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GestionnaireRepository extends JpaRepository<Gestionnaire , String> {
}
