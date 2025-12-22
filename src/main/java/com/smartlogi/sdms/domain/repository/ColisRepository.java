package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.Colis;
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColisRepository extends JpaRepository<Colis, String>, JpaSpecificationExecutor<Colis> {

    Page<Colis> findAllByClientExpediteurId(@Param("idClient") String idClient, Pageable pageable);

    @Query("SELECT c FROM Colis c JOIN FETCH c.destinataire WHERE c.clientExpediteur.id = :clientId")
    List<Colis> findByClientExpediteur_Id(String clientId);

    boolean existsByClientExpediteur_Id(String idClient);

    @Query("SELECT c FROM Colis c WHERE " +
            "(:statut IS NULL OR c.statut = :statut) AND " +
            "(:priorite IS NULL OR c.priorite = :priorite) AND " +
            "(:ville IS NULL OR LOWER(c.villeDestination) LIKE LOWER(CONCAT('%', cast(:ville as string), '%'))) AND " +
            "(:description IS NULL OR LOWER(c.description) LIKE LOWER(CONCAT('%', cast(:description as string), '%'))) AND " +
            "(:trackingCode IS NULL OR LOWER(c.trackingCode) LIKE LOWER(CONCAT('%', cast(:trackingCode as string), '%'))) AND " + // 👈 AJOUT
            "(:expediteurId IS NULL OR c.clientExpediteur.id = :expediteurId)")
    Page<Colis> rechercheAvancee(
            @Param("statut") StatusColis statut,
            @Param("priorite") PriorityColis priorite,
            @Param("ville") String ville,
            @Param("description") String description,
            @Param("trackingCode") String trackingCode, // 👈 AJOUT
            @Param("expediteurId") String expediteurId,
            Pageable pageable
    );

    // CORRECTION: Remplacer le Derived Query invalide par un JPQL explicite
    @Query("SELECT c FROM Colis c WHERE c.livreurCollecte.id = :idLivreur OR c.livreurLivraison.id = :idLivreur")
    List<Colis> findAllByLivreurId(@Param("idLivreur") String idLivreur);

    Optional<Colis> findByTrackingCode(String trackingCode);

}