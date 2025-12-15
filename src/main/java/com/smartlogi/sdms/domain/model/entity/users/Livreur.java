package com.smartlogi.sdms.domain.model.entity.users;

import com.smartlogi.sdms.domain.model.entity.Colis; // Import requis
import com.smartlogi.sdms.domain.model.entity.Zone;
import com.smartlogi.sdms.domain.model.enums.StatusLivreur;
import com.smartlogi.sdms.domain.model.vo.Vehicule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List; // Import requis

@Setter
@Getter
@Entity
@Table(name = "livreur")
@PrimaryKeyJoinColumn(name = "user_id")
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Livreur extends BaseUser {

    @Enumerated(EnumType.STRING)
    @Column(name = "status_livreur")
    private StatusLivreur statusLivreur;

    @Embedded
    private Vehicule vehicule;

    @Column(name = "dateDepart")
    private LocalDateTime dateDepart;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_assignee_id")
    private Zone zoneAssignee;

    // --- CORRECTION DE LA RELATION ---

    // AVANT (Ce qui cause l'erreur) :
    // @OneToMany(mappedBy = "livreur")
    // private List<Colis> colisAssignes;

    // APRÈS (Correction) :
    // Mappé vers le champ "livreurCollecte" dans Colis.java
    @OneToMany(mappedBy = "livreurCollecte" , fetch = FetchType.LAZY)
    private List<Colis> colisEnCollecte;

    // Mappé vers le champ "livreurLivraison" dans Colis.java
    @OneToMany(mappedBy = "livreurLivraison" , fetch = FetchType.LAZY)
    private List<Colis> colisEnLivraison;
}