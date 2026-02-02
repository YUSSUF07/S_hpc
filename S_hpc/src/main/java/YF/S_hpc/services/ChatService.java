package YF.S_hpc.services;

import YF.S_hpc.agents.AiAssistantTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    interface Assistant {
        String chat(String userMessage);
    }

    private final Assistant assistant;
    private final MessageWindowChatMemory chatMemory;

    public ChatService(
            ChatLanguageModel chatModel,
            AiAssistantTools tools) {

        this.chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        // ✅ Configuration correcte avec tools
        this.assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(chatMemory)
                .tools(tools) // ← Enregistrement des tools
                .build();

        System.out.println("✅ ChatService initialisé avec tools");
    }

    public String chat(String userMessage) {
        System.out.println("📩 Message reçu: " + userMessage);
        String response = assistant.chat(userMessage);
        System.out.println("📤 Réponse envoyée: " + response);
        return response;
    }

    public void resetMemory() {
        chatMemory.clear();
        System.out.println("🗑️ Mémoire effacée");
    }
}