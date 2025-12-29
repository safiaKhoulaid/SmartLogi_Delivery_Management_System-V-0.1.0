package com.smartlogi.sdms.infrastructure.configuration;

import com.smartlogi.sdms.application.service.JWTService;
import com.smartlogi.sdms.domain.model.entity.users.BaseUser;
import com.smartlogi.sdms.domain.model.enums.AuthProvider;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final BaseUserRepository userRepository;

    @Value("${application.security.oauth2.frontend-url:http://localhost:4200/login-success}")
    private String frontendUrl;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String providerId = oAuth2User.getName();

        BaseUser user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setProvider(AuthProvider.OKTA);
                    existingUser.setProviderId(providerId);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    BaseUser newUser = BaseUser.builder()
                            .email(email)
                            .firstName(firstName)
                            .lastName(lastName)
                            .role(Role.USER)
                            .provider(AuthProvider.OKTA)
                            .providerId(providerId)
                            .password("")
                            .build();
                    return userRepository.save(newUser);
                });

        String jwtToken = jwtService.generateToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", jwtToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private BaseUser saveOrUpdateUser(String email, String firstName, String lastName) {
        Optional<BaseUser> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {

            BaseUser newUser = BaseUser.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(Role.USER)
                    .password("")
                    .build();
            return userRepository.save(newUser);
        }
    }
}