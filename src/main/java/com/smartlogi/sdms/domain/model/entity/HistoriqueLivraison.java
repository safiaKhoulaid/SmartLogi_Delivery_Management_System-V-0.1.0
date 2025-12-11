package com.smartlogi.sdms.domain.model.entity;


import com.smartlogi.sdms.domain.model.enums.StatusColis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_livraison")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut")
    private StatusColis nouveauStatut;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;

    @Column(name = "commentaire", length = 500)
    private String commentaire; // Information additionnelle du livreur/gestionnaire

    // --- Relation Many-to-One ---

    // Le colis concerné par ce changement de statut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colis_id", nullable = false)
    private Colis colis;

    // Optionnel : L'utilisateur qui a effectué le changement de statut
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "utilisateur_id")
    // private BaseUser utilisateur;
}
