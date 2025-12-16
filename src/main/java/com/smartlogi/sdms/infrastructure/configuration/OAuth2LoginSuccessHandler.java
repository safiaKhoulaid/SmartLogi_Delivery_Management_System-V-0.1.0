package com.smartlogi.sdms.infrastructure.configuration;

import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final BaseUserRepository userRepository;

    @Value("${application.security.oauth2.frontend-url:http://localhost:4200/login-success}")
    private String frontendUrl; // Fin ghanseftuh f Angular

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. Jib l'info dyal User mn Google
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        // 2. Chuf wach had User kayn f base de données, ila la créer wahd jdid
        BaseUser user = saveOrUpdateUser(email, firstName, lastName);

        // 3. Générer JWT Token bhalla rah dkhl b login/password
        String jwtToken = jwtService.generateToken(user); // Ta2kdi anaha kat9bl User entity

        // 4. Seftih l Angular m3a Token f URL
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", jwtToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private BaseUser saveOrUpdateUser(String email, String firstName, String lastName) {
        Optional<BaseUser> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // Ila kan déjà kayn, ymkn t-update lih smya ila bghiti
            return existingUser.get();
        } else {
            // Ila makanch, créer user jdid
            BaseUser newUser = BaseUser.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(Role.USER) // Awla role par défaut 3ndk
                    .password("") // Password khawi hit jay mn Google
                    .build();
            return userRepository.save(newUser);
        }
    }
}