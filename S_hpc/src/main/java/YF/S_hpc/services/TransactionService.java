// services/TransactionService.java
package YF.S_hpc.services;

import YF.S_hpc.entities.Transaction;
import YF.S_hpc.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des transactions.
 * Aucune logique IA ici - juste l'accès aux données.
 */
@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }

    public Optional<Transaction> findById(Long id) {
        return repository.findById(id);
    }

    public Transaction save(Transaction transaction) {
        return repository.save(transaction);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public long count() {
        return repository.count();
    }
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

}