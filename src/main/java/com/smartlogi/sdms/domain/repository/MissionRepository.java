package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRepository extends JpaRepository<Mission, String> {
    void deleteAllByColisId(String colisId);
}
