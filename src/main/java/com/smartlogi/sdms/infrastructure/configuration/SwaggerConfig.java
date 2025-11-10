package com.smartlogi.sdms.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestion des Tickets")
                        .version("1.0.0")
                        .description("Documentation de l'API du projet Spring Boot")
                        .contact(new Contact()
                                .name("Sofi Dev")
                                .email("contact@sofi.dev"))
                        .license(new License().name("Apache 2.0")));
    }
}
