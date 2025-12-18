package com.smartlogi.sdms.domain.repository;

import com.smartlogi.sdms.domain.model.entity.BlackListToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface BlackListTokenRepository  extends CrudRepository<BlackListToken, String> {
}
