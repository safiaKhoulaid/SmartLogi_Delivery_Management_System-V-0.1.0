package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientExpediteurRepository extends JpaRepository<ClientExpediteur, String> {
}
