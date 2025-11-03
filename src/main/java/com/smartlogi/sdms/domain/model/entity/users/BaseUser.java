package com.smartlogi.sdms.domain.model.entity.users;

import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "base_user") // nommer le table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = "relations")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_utilisateur", discriminatorType = DiscriminatorType.STRING)

public abstract class BaseUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role")
    private String role;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "numero", column = @Column(name = "street_number")),
        @AttributeOverride(name = "rue", column = @Column(name = "street")),
        @AttributeOverride(name = "ville", column = @Column(name = "city")),
        @AttributeOverride(name = "codePostal", column = @Column(name = "postal_code")),
        @AttributeOverride(name = "pays", column = @Column(name = "country"))
    })
    private Adresse adresse; // Pas besoin de @Column sur un @Embedded

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "codePays", column = @Column(name = "country_code")),
        @AttributeOverride(name = "numero", column = @Column(name = "number"))
    })
    private Telephone telephone; // Pas besoin de @Column sur un @Embedded

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role != null ? role : "USER"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}