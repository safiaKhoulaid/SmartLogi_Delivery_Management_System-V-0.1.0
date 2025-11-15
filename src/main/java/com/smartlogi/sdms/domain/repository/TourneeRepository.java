package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.Tournee;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("""
    select distinct t
    from Tournee t
    join fetch t.livreur l
    left join fetch l.zoneAssignee za
    left join fetch t.zone z
    left join fetch t.livraisons c
    left join fetch c.zoneDestination zd
    where t.id = :id
    """)
    Optional<Tournee> findByIdWithAssociations(@Param("id") long id);



        @Query("""
        select distinct t
        from Tournee t
        join fetch t.livreur l
        left join fetch l.zoneAssignee za
        left join fetch t.livraisons c
        left join fetch c.zoneDestination zd
        where l.id = :livreurId
        """)
        Page<Tournee> findByLivreurIdWithAssociations(@Param("livreurId") String livreurId , Pageable pageable);
    }


