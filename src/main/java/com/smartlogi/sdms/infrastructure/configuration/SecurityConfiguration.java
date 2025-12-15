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
import org.springframework.security.web.AuthenticationEntryPoint; // Import
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler; // Import
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    // 👇 1. Injection dyal les handlers li sawbna
    // (Spring ghadi y-lqahom hit drti fihom @Component wla @Bean)
    private final AuthenticationEntryPoint authEntryPoint; // L 401 (Login naqess/ghalet)
    private final AccessDeniedHandler accessDeniedHandler; // L 403 (Role naqess)

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

                // 👇 2. HNA FIN KAN-RBTU L-HANDLERS M3A SPRING SECURITY
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint) // Traitement dyal 401
                        .accessDeniedHandler(accessDeniedHandler) // Traitement dyal 403
                )

                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\": \"Déconnexion réussie.\"}");
                        })
                );

        return http.build();
    }
}