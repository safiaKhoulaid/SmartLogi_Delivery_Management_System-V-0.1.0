package com.smartlogi.sdms.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.StatutTournee; // Vous devez créer cet enum
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tournees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tournee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private LocalDate dateTournee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTournee statut;

    @Column
    private Double distanceTotaleKm;

    @Column
    private Double dureeEstimeeHeures;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "livreur_id", nullable = false)
    private Livreur livreur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone; // Zone de la tournée, utilisée pour le point de départ (dépôt)

    @OneToMany(mappedBy = "tournee", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("ordreLivraison ASC") // Les colis seront toujours triés par l'ordre optimisé
    @JsonManagedReference
    private List<Colis> livraisons; // Votre "listeDeLivraisons"
}