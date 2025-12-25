package com.smartlogi.sdms.application.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CiAiService {
    private final ChatClient chatClient;

    public CiAiService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                        Tu es un ingénieur DevOps. 
                        1. REASON: Analyse 'build.log'. 
                        2. Si le build est RÉUSSI (SUCCESS) : Fais une petite revue de code (Code Review) sur les derniers changements.
                        3. Si le build a ÉCHOUÉ (FAILED) : Trouve l'erreur et propose un fix.
                        """)
                .defaultFunctions("readBuildLog", "readSourceCode")
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