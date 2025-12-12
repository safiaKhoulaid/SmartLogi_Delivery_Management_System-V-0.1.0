package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.domain.exception.ResourceNotFoundException;
import com.smartlogi.sdms.domain.model.entity.RefreshToken;
import com.smartlogi.sdms.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;



    public RefreshToken createRefreshToken(String username) {

        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken(token, username);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findById(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh Token not found in Redis!"));
    }

    public RefreshToken verifyExpiration(RefreshToken token) {

        return token;
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.deleteById(token);
    }
}