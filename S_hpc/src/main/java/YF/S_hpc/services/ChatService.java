package YF.S_hpc.services;

import YF.S_hpc.agents.AiAssistantTools;
import YF.S_hpc.agents.AiAssistantTools;
import YF.S_hpc.agents.TransactionAiAgent;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatLanguageModel chatModel;
    private final AiAssistantTools tools;
    private TransactionAiAgent agent;
    private final StreamingChatLanguageModel streamingModel;

    public ChatService(
            ChatLanguageModel chatModel,
            AiAssistantTools tools, StreamingChatLanguageModel streamingModel) {
        this.chatModel = chatModel;
        this.tools = tools;
        this.streamingModel = streamingModel;
    }

    @PostConstruct
    public void init() {
        // Mémoire conversationnelle (garde les 20 derniers messages)
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        // Construction de l'agent avec tous ses composants
        this.agent = AiServices.builder(TransactionAiAgent.class)
                .chatLanguageModel(chatModel)
                .streamingChatLanguageModel(streamingModel)
                .chatMemory(chatMemory)
                .tools(tools)
                .build();
    }

    /**
     * Point d'entrée principal pour communiquer avec l'agent IA.
     */
    public String chat(String userMessage) {
        return agent.chat(userMessage);
    }

    public TokenStream chatStream(String userMessage) {
        return agent.chatStream(userMessage);
    }
    /**
     * Réinitialiser la mémoire de conversation.
     */
    public void resetMemory() {
        init(); // Recrée l'agent avec une nouvelle mémoire
    }
}
