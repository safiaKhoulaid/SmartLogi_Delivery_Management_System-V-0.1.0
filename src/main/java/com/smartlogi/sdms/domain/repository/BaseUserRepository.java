package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaseUserRepository extends JpaRepository<BaseUser, String> {

    //Méthode pout récupérer un utilisateur par son email
    Optional<BaseUser> findByEmail(String username);
}
