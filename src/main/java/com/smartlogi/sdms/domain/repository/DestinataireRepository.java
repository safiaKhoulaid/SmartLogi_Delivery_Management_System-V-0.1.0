package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DestinataireRepository extends JpaRepository<Destinataire, String> {

    public Optional<Destinataire> findByEmail(String email);

    Page<Destinataire> findAllByClientExpediteurId(String clientExpediteurId, Pageable pageable);
}
