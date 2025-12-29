package com.smartlogi.sdms.domain.model.entity.users;

import com.smartlogi.sdms.domain.model.enums.AuthProvider;
import com.smartlogi.sdms.domain.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;

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
@ToString(callSuper = true)
@Inheritance(strategy = InheritanceType.JOINED)
@Accessors(chain = true)
public class BaseUser extends PersonInfo implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private String id;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL; // par défaut LOCAL

    private String providerId;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        BaseUser baseUser = (BaseUser) o;
        return id != null && Objects.equals(id, baseUser.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }



}