package com.smartlogi.sdms.infrastructure.configuration.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

@Configuration
public class CiTools {

    public record LogRequest(String path) {}
    public record SourceCodeRequest(String fileName) {}

    @Bean
    @Description("Lire le fichier build.log pour voir l'erreur de compilation ou de test")
    public Function<LogRequest, String> readBuildLog() {
        return request -> {
            try {
                return Files.readString(Path.of("build.log"));
            } catch (Exception e) {
                return "Erreur de lecture du log : " + e.getMessage();
            }
        };
    }

    @Bean
    @Description("Lire le contenu d'un fichier source Java pour analyser le code")
    public Function<SourceCodeRequest, String> readSourceCode() {
        return request -> {
            try {
                return Files.readString(Path.of("src/main/java/" + request.fileName()));
            } catch (Exception e) {
                return "Fichier introuvable : " + request.fileName();
            }
        };
    }
}