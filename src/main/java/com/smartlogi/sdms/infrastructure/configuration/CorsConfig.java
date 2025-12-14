package com.smartlogi.sdms.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 1. Hada kay-3ni: Tabbaq CORS 3la ga3 les endpoints API
                        .allowedOrigins("http://localhost:4200", "http://le-lien-dyal-frontend-f-aws.com") // 2. Hna fin kaina l-khedma
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 3. Methods msmou7a
                        .allowedHeaders("*")
                        .allowCredentials(true); // Ila knti kat-sta3mli cookies wla authentication headers
            }
        };
    }
}