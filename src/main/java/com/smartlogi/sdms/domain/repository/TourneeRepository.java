package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.Tournee;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface TourneeRepository extends JpaRepository<Tournee, Long> {

    List<Tournee> findByLivreurId(String livreurId);

    List<Tournee> findByDateTournee(LocalDate date);

    List<Tournee> findByZoneId( String zoneId);

    Optional<Tournee> findByIdAndLivreurId(long id, String livreurId);
}