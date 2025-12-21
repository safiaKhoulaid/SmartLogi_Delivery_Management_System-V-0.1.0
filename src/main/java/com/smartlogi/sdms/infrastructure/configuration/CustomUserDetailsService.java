package com.smartlogi.sdms.infrastructure.configuration;

import com.smartlogi.sdms.domain.model.entity.Permission;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import com.smartlogi.sdms.domain.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService , UserDetailsPasswordService {

    private final BaseUserRepository repository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        BaseUser user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Permission> permissions = permissionRepository.findAllByRoleName(user.getRole().name());


        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getName())));


        return new User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );

    }

    /**
     * @param user
     * @param newPassword
     * @return
     */
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        BaseUser entity = repository.findByEmail(user.getUsername()).orElseThrow();
        entity.setPassword(newPassword);
        repository.save(entity);
        return entity;
    }
}