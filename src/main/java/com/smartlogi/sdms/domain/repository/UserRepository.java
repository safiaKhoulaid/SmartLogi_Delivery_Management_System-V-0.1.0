package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<BaseUser, String> {
    Optional<BaseUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
