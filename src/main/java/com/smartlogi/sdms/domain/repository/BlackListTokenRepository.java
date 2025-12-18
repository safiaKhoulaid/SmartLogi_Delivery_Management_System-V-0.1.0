package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.BlackListToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListTokenRepository  extends JpaRepository<BlackListToken, String> {
}
