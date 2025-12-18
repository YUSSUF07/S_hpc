package YF.S_hpc.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import YF.S_hpc.repository.TransactionRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TransactionController {

    private TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/transactions")
    public Object getAllTransactions() {
        return transactionRepository.findAll();
    }
}
