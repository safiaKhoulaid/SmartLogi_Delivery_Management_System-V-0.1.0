package com.smartlogi.sdms.application.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Service
public class LogisticsAiService {

    private final ChatClient chatClient;

    public LogisticsAiService(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultSystem("""
                        Tu es l'assistant logistique de SmartLogi.
                        Tes consignes de réponse :
                        1. POLITESSE : Reste toujours courtois et professionnel.
                        2. CONCISION : Sois très direct. Pas de phrases inutiles.
                        3. BRIÈVETÉ : Limite tes réponses à 2 ou 3 phrases maximum.
                        4. OUTILS : Utilise 'getColisStatusTool' uniquement si un code de suivi est fourni.
                        5. INCONNU : Si l'info manque, réponds simplement : 'Désolé, je ne trouve pas d'information pour ce colis.'
                        """)
                .defaultFunctions("getColisStatusTool")
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    public String askAssistant(String question, String chatId) {
        return chatClient.prompt()
                .user(question)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .content();
    }
}