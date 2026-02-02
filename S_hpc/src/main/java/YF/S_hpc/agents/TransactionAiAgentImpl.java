package YF.S_hpc.agents;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.springframework.stereotype.Component;

@Component
    public class TransactionAiAgentImpl {

        private final TransactionAiAgent assistant;

        public TransactionAiAgentImpl(ChatLanguageModel chatModel,
                                      StreamingChatLanguageModel streamingChatModel,
                                      ContentRetriever contentRetriever,
                                      AiAssistantTools tools) {

            this.assistant = AiServices.builder(TransactionAiAgent.class)
                    .chatLanguageModel(chatModel)
                    .streamingChatLanguageModel(streamingChatModel)
                    .contentRetriever(contentRetriever)   // 🔥 RAG ACTIF
                    .tools(tools)                          // 🔥 TOOLS ACTIFS
                    .build();
        }

        public String chat(String message) {
            return assistant.chat(message);
        }

        public TokenStream chatStream(String message) {
            return assistant.chatStream(message);
        }

}
