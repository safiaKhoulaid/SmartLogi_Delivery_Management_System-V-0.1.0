package com.smartlogi.sdms.domain.model.entity.users;

import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "base_user")

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString()
@Inheritance(strategy = InheritanceType.JOINED)
@Accessors(chain = true)

public class BaseUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private String id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Embedded
    private Adresse adresse;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "codePays", column = @Column(name = "telephone_code_pays")),
            @AttributeOverride(name = "nombre", column = @Column(name = "telephone_numero"))
    })
    private Telephone telephone;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        // Utiliser Hibernate.getClass(o) pour prendre en compte les Proxies
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;

        // Cast sécurisé après vérification de la classe
        BaseUser baseUser = (BaseUser) o;

        // Deux entités sont égales si et seulement si elles ont le même ID
        // ET que cet ID n'est pas null (entité persistante).
        // Si l'ID est null, l'objet n'est pas encore persistant et l'égalité doit être refusée
        // pour ne pas causer de problème lors de l'ajout à un Set/Map.
        return id != null && Objects.equals(id, baseUser.id);
    }

    /**
     * Génère un hash code constant si l'objet n'est pas encore persistant,
     * sinon utilise l'ID pour le hachage.
     * Base le hachage sur l'ID si disponible, sinon sur un code constant (éviter la regénération du hash)
     * pour les entités non persistantes dans les collections Set/Map.
     */
    @Override
    public int hashCode() {
        // Retourne un hash basé sur l'ID s'il est non-null, sinon un code constant.
        // Utiliser un code constant (ex: 31) pour les objets non persistants est une technique
        // pour s'assurer qu'ils restent au même endroit dans les collections Set/Map,
        // même s'ils ne sont pas réellement égaux.
        return id != null ? id.hashCode() : 31;
    }

    /**
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * @return
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    /**
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    /**
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    /**
     * @return
     */
    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}