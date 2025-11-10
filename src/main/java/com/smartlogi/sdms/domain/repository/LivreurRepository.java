package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.StatusLivreur;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivreurRepository extends JpaRepository<Livreur, String> {


    List<Livreur> findAllByZoneAssigneeIdAndStatusLivreur(String zoneAssigneeId, StatusLivreur statusLivreur);
 }
