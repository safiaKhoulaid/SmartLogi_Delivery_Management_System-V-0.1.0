package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, String>{

    Optional<Zone> findByVilleAndCodePostal(String villeDestination , String codePostal);
}
