package com.smartlogi.sdms.infrastructure.configuration;

import com.smartlogi.sdms.application.service.JWTService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // 👈 AJOUTER CETTE LIGNET
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    @Bean
    public JWTAuthFilter jwtAuthFilter() {
        return new JWTAuthFilter(jwtService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Vos règles existantes autorisent déjà /api/v1/auth/**
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(
                                "/v1/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html**",
                                "/swagger-ui/index.html**/**",
                                "/actuator/**",
                                "/api/v1/missions/create"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)

                // --- 🔻 DÉBUT DE L'AJOUT POUR LE LOGOUT ---
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout") // 1. Définir l'URL de déconnexion
                        .logoutSuccessHandler((request, response, authentication) -> {

                            // 2. Nettoyer le contexte de sécurité pour cette requête
                            SecurityContextHolder.clearContext();

                            // 3. Envoyer une réponse JSON claire au client
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Déconnexion réussie.\"}");
                        })
                );
        // --- 🔺 FIN DE L'AJOUT ---

        return http.build();
    }

}
