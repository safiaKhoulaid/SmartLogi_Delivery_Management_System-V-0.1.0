package com.smartlogi.sdms.domain.model.entity.users;

import com.smartlogi.sdms.domain.model.entity.Colis;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "destinataire")
@PrimaryKeyJoinColumn(name = "user_id")
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Destinataire extends BaseUser {

    // SUPPRIMER : @Column(name = "expediteur_id", nullable = false)
    // private String expediteurId;

    // AJOUTER : La relation Many-to-One vers le ClientExpediteur qui a créé ce destinataire
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediteur_id", nullable = false) // Mappe vers la colonne existante
    private ClientExpediteur clientExpediteur; // Nouvelle propriété pour la relation

    // L'entité Destinataire ne devrait pas avoir la liste des colis expédiés,
    // car seul le ClientExpediteur peut expédier. Cette partie du code est incorrecte
    // ou provient d'une mauvaise fusion des entités.

    /*
    @OneToMany(mappedBy = "clientExpediteur",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    private List<Colis> colisExpedies;
    */
}