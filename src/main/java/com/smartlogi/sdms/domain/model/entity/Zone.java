package com.smartlogi.sdms.domain.model.entity;

import com.smartlogi.sdms.domain.model.entity.users.Livreur;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "zone")
@Getter
@Setter
@RequiredArgsConstructor // Inclut @Getter, @Setter, @ToString, @EqualsAndHashCode
@Builder // Permet la création d'instances de manière sécurisée
@NoArgsConstructor // Requis par JPA et Builder
@AllArgsConstructor // Requis par Builder
@ToString
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // Utilisation de Long pour les IDs séquentiels/auto-incrémentés

    @Column(name = "nom", unique = true)
    private String nom; // Nom de la zone (ex : 'Rabat Centre', 'Casablanca Sud')


    @Column(name = "ville")
    String ville;

    @Column(name = "code_postal")
    private String codePostal; // Un code postal ou une plage de codes postaux associés à cette zone

    // --- Relations (One-to-Many) ---

    // 1. Les Livreuers assignés à cette Zone
    // La zone est la source d'information sur les livreurs qui y travaillent.
    @OneToMany(mappedBy = "zoneAssignee", fetch = FetchType.EAGER)
    private List<Livreur> livreurs;

    // 2. Les Colis qui ont cette Zone comme destination
    // Ceci est crucial pour le regroupement et la planification des livraisons par zone.
    @OneToMany(mappedBy = "zoneDestination", fetch = FetchType.EAGER)
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