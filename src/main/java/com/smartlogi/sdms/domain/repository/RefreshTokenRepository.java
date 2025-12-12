package com.smartlogi.sdms.domain.repository;


import com.smartlogi.sdms.domain.model.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByUsername(String username);
}
