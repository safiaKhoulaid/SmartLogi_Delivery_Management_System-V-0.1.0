package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.service.ai.CiAiService;
import com.smartlogi.sdms.application.service.ai.LogisticsAiService;
import com.smartlogi.sdms.infrastructure.configuration.ai.CiTools;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final LogisticsAiService aiService;
    private final CiAiService ciAiService ;

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {

        String question = request.get("question");
        String chatId = request.getOrDefault("chatId", "default-session");
        String reponse = aiService.askAssistant(question, chatId);
        return Map.of("response", reponse);
    }


    @PostMapping(value = "/analyze-cicd", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String analyze(@RequestBody String logContent) {
        return ciAiService.solveBuildError(logContent);
    }
}