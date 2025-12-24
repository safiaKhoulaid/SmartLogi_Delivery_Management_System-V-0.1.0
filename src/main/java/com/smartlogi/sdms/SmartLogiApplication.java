package com.smartlogi.sdms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
@EnableAsync
@Slf4j
public class SmartLogiApplication {

    public static void main(String[] args) {
        log.info("Starting SmartLogiApplication");
        String profile = System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "dev");
        log.info("SPRING_PROFILES_ACTIVE = {}", profile);
        System.setProperty("spring.profiles.active", profile);
        log.info("welcome to your home");
        SpringApplication.run(SmartLogiApplication.class, args);

       }

    @Bean
    public CommandLineRunner printFilters(FilterChainProxy filterChainProxy) {
        return args -> {
            System.out.println("====== LISTE DES FILTRES SPRING SECURITY ======");

            // Hada kayjbed ga3 s-snasel (Chains)
            for (SecurityFilterChain chain : filterChainProxy.getFilterChains()) {

                chain.getFilters().forEach(filter -> {
                    System.out.println("-> " + filter.getClass().getSimpleName());
                });
            }
            System.out.println("===============================================");
        };   }
}
