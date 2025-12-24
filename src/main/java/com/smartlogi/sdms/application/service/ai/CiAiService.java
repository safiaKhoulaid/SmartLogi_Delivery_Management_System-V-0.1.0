package com.smartlogi.sdms.application.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CiAiService {
    private final ChatClient chatClient;

    public CiAiService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                Tu es un ingénieur DevOps expert en ReAct. 
                Si un build échoue :
                1. REASON: Pourquoi ça a échoué ? (Appelle 'readBuildLog')
                2. ACT: Analyse le code source concerné (Appelle 'readSourceCode')
                3. OBSERVATION: Propose un correctif précis.
                """)
                .defaultFunctions("readBuildLog", "readSourceCode")
                .build();
    }

    public String solveBuildError() {
        return chatClient.prompt()
                .user("Le build a échoué. Analyse le log et propose une solution.")
                .call()
                .content();
    }
}