package com.smartlogi.sdms.application.service;

import com.smartlogi.sdms.domain.model.entity.BlackListToken;
import com.smartlogi.sdms.domain.repository.BlackListTokenRepository;
import com.smartlogi.sdms.domain.repository.RefreshTokenRepository; //
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final BlackListTokenRepository blackListTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTService jwtService; //

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        // 1. نجبدو Header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        // 2. نجبدو Token
        jwt = authHeader.substring(7);

        // 3. نحرقو Access Token (Blacklist)
        // تأكدي أن BlackListToken عندها Constructor كياخد String
        BlackListToken blackListedToken = new BlackListToken(jwt);
        blackListTokenRepository.save(blackListedToken);

        // 4. نمسحو Refresh Token (ديال هاد المستخدم)
        try {
            String userEmail = jwtService.extractUserEmail(jwt);
            var storedToken = refreshTokenRepository.findByUsername(userEmail);
            if (storedToken.isPresent()) {
                refreshTokenRepository.delete(storedToken.get());
                log.info("Refresh token supprimé pour : {}", userEmail);
            }
        } catch (Exception e) {
            log.error("Erreur lors du logout", e);
        }

        // 5. نخويو Context
        SecurityContextHolder.clearContext();
    }
}