package com.smartlogi.sdms.infrastructure.configuration.ai;

import com.smartlogi.sdms.application.dto.ai.ColisAiRequest;
import com.smartlogi.sdms.application.dto.ai.ColisAiResponse;
import com.smartlogi.sdms.domain.repository.ColisRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class AiToolsConfig {

    @Bean
    @Description("Chercher le statut et la ville d'un colis via son code de suivi")
    public Function<ColisAiRequest, ColisAiResponse> getColisStatusTool(ColisRepository colisRepository) {
        return request -> {
            if (request.trackingCode() == null || request.trackingCode().isBlank()) {
                return new ColisAiResponse("CODE_MANQUANT", "N/A");
            }
            return colisRepository.findByTrackingCode(request.trackingCode()).map(colis -> new ColisAiResponse(colis.getStatut().toString(), colis.getVilleDestination()))


                    .orElse(new ColisAiResponse("INTROUVABLE", "INCONNUE"));
        };
    }
}