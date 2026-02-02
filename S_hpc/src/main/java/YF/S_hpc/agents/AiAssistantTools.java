// services/ChatService.java
package YF.S_hpc.agents;

import YF.S_hpc.entities.Transaction;
import YF.S_hpc.services.TransactionService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AiAssistantTools {

    private final TransactionService transactionService;

    public AiAssistantTools(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Tool("Obtenir le nombre total de transactions")
    public long countTransactions() {
        return transactionService.count();
    }

    @Tool("Lister toutes les transactions")
    public List<Transaction> getAllTransactions() {
        return transactionService.findAll();
    }

    @Tool("Rechercher une transaction spécifique par son identifiant numérique")
    public Transaction findTransactionById(@P("id de la transaction (nombre entier)") Long id) {
        log.info("🔍 Recherche de la transaction avec ID: {}", id);

        // Validation
        if (id == null || id <= 0) {
            log.warn("⚠️ ID invalide: {}", id);
            throw new IllegalArgumentException("L'ID de la transaction doit être un nombre positif valide");
        }

        return transactionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction introuvable avec l'ID: " + id));
    }

    @Tool("Calculer le montant total des transactions")
    public double calculateTotalAmount() {
        return transactionService.findAll()
                .stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}
