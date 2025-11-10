package com.smartlogi.sdms.domain.model.entity;


import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.enums.MissionType;
import com.smartlogi.sdms.domain.model.enums.StatutMission;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutMission statut;

    // 🔹 Adresse d’intervention (origine)
    @Embedded
    private Adresse origineAdresse;

    // 🔹 Adresse d’intervention (destination)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "rue", column = @Column(name = "destination_rue")),
            @AttributeOverride(name = "numero", column = @Column(name = "destination_numero")),
            @AttributeOverride(name = "ville", column = @Column(name = "destination_ville")),
            @AttributeOverride(name = "codePostal", column = @Column(name = "destination_code_postal")),
            @AttributeOverride(name = "pays", column = @Column(name = "destination_pays")) ,
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude")), // <-- Ajouté
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude")) // <-- Corrigé
      })
    private Adresse destinationAdresse;

    @Column(name = "date_prevue", nullable = false)
    private LocalDateTime datePrevue;

    @Column(name = "date_effective")
    private LocalDateTime dateEffective;

    @Column(length = 500)
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livreur_id", nullable = false)
    private Livreur livreur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colis_id", nullable = false)
    private Colis colis;
}
