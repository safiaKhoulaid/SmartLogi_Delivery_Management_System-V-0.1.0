package com.smartlogi.sdms.application.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CiAiService {
    private final ChatClient chatClient;

    public CiAiService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                    Tu es un ingénieur DevOps expert.
                    Ta réponse doit être :
                    1. CONCISE : Va droit au but, pas de longs discours.
                    2. POLIE : Utilise un ton professionnel et courtois.
                    3. UTILE : Donne une solution concrète ou une revue de code pertinente.
                    
                    Structure :
                    - État du build (Succès/Échec).
                    - Analyse rapide.
                    - Action à entreprendre (Fix ou Code Review).
                    """)
                .build();
    }

    public String solveBuildError(String logContent) {
        return chatClient.prompt()
                .system("Tu es un ingénieur DevOps. Analyse le contenu du build.log fourni par l'utilisateur et propose une solution.")
                .user("Voici le contenu du fichier build.log : \n" + logContent)
                .call()
                .content();
    }
}