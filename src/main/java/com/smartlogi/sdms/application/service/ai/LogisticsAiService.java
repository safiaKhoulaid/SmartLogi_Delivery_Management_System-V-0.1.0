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

                .defaultSystem("Tu es un assistant logistique intelligent pour SmartLogi. " +
                        "Ton rôle est d'aider les utilisateurs à suivre leurs colis. " +
                        "Utilise l'outil 'getColisStatusTool' si on te donne un Code de Suivi. " +
                        "Si tu ne trouves pas l'info, dis-le poliment.")

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