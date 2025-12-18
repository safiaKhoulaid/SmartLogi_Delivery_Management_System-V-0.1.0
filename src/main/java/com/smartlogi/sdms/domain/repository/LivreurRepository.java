package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.StatusLivreur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivreurRepository extends JpaRepository<Livreur, String> {


    List<Livreur> findAllByZoneAssigneeIdAndStatusLivreur(String zoneAssigneeId, StatusLivreur statusLivreur);

    Optional<Livreur> findByEmail(String email);
}
