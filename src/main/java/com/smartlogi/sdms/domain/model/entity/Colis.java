package com.smartlogi.sdms.domain.model.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.smartlogi.sdms.domain.model.entity.users.ClientExpediteur;
import com.smartlogi.sdms.domain.model.entity.users.Destinataire;
import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.PriorityColis;
import com.smartlogi.sdms.domain.model.enums.StatusColis;
import com.smartlogi.sdms.domain.model.vo.Poids;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter// Inclut @Getter, @Setter, @ToString, @EqualsAndHashCode
@Builder // Permet la création d'instances de manière sécurisée
@NoArgsConstructor // Requis par JPA et Builder
@AllArgsConstructor // Requis par Builder

@Entity
@Table(name = "colis")

public class Colis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "description")
    private String description;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long  numero ;

    @Embedded
    private Poids poids;

    @Column(name = "ville_destination")
    private String villeDestination;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    // --- Attributs de Logistique (Enums) ---

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatusColis statut;

    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false)
    private PriorityColis priorite;

    // --- Relations Many-to-One (Clés Étrangères) ---

    // Le client qui a initié l'envoi
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_expediteur_id", nullable = false)
    private ClientExpediteur clientExpediteur;

    // La personne qui reçoit le colis
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Destinataire destinataire;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    // Le livreur actuellement assigné à ce colis (peut être NULL si EN_STOCK)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "livreur_collecte_id")
    private Livreur livreurCollecte;

    // La zone logistique de destination du colis
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "livreur_livraison_id")
    private Livreur livreurLivraison;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_destination_id", nullable = false)
    private Zone zoneDestination;

    @OneToMany(mappedBy = "colis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HistoriqueLivraison> historiqueLivraisons;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournee_id")
    @JsonBackReference // Empêche les boucles infinies lors de la sérialisation JSON
    private Tournee tournee;

    @Column(name = "ordre_livraison")
    private Integer ordreLivraison;

}
