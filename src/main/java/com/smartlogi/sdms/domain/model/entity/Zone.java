package com.smartlogi.sdms.domain.model.entity;

import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "zone") // "zone" est souvent un mot-clé, "zones" est plus sûr
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // Gardé en String (UUID)

    @Column(name = "nom", unique = true)
    private String nom; // Nom de la zone (ex : 'Rabat Centre', 'Casablanca Sud')

    @Column(name = "ville")
    String ville;

    @Column(name = "code_postal")
    private String codePostal;

    // ⬇️ --- AJOUT REQUIS --- ⬇️
    // Adresse du dépôt/entrepôt qui gère cette zone.
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "rue", column = @Column(name = "depot_rue")), // (Vérifiez si "depot_numero" et les autres sont corrects)
            @AttributeOverride(name = "ville", column = @Column(name = "depot_ville")),
            @AttributeOverride(name = "numero", column = @Column(name = "depot_numero")),
            @AttributeOverride(name = "codePostal", column = @Column(name = "depot_code_postal")),
            @AttributeOverride(name = "pays", column = @Column(name = "depot_pays")),


            @AttributeOverride(name = "latitude", column = @Column(name = "depot_attitide")), // <-- CORRIGÉ (selon votre typo)
            @AttributeOverride(name = "longitude", column = @Column(name = "depotlangtitude")) // <-- CORRIGÉ (selon votre typo)
    })
    private Adresse adresseDepot;

    // ⬆️ --- FIN DE L'AJOUT --- ⬆️


    // --- Relations (One-to-Many) ---

    @OneToMany(mappedBy = "zoneAssignee", fetch = FetchType.LAZY) // <-- Corrigé en LAZY
    @ToString.Exclude // <-- Ajouté pour éviter les boucles
    private List<Livreur> livreurs;

    @OneToMany(mappedBy = "zoneDestination", fetch = FetchType.LAZY) // <-- Corrigé en LAZY
    @ToString.Exclude // <-- Ajouté pour éviter les boucles
    private List<Colis> colis;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Zone zone = (Zone) o;
        return getId() != null && Objects.equals(getId(), zone.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}