package com.smartlogi.sdms.presentation.controller;

import com.smartlogi.sdms.application.service.ai.LogisticsAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final LogisticsAiService aiService;

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {

        String question = request.get("question");
        String chatId = request.getOrDefault("chatId", "default-session");
        String reponse = aiService.askAssistant(question, chatId);
        return Map.of("response", reponse);
    }
}