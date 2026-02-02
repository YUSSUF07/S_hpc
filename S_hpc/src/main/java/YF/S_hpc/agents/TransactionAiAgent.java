package YF.S_hpc.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;


public interface TransactionAiAgent {

    @SystemMessage("""
        Tu es un assistant expert en transactions financières.
        Tu peux accéder aux données via les outils disponibles.
        Réponds de manière claire, précise et professionnelle.
        Si tu utilises des outils, explique ce que tu fais.
        """)
    String chat(@UserMessage String query);

    @SystemMessage("Tu es un assistant expert en transactions financières.")
    TokenStream chatStream(@UserMessage String query);
}
