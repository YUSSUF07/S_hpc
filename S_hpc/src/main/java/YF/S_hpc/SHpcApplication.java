package YF.S_hpc;

import entities.Transaction;
import entities.TransactionStatus;
import entities.TransactionType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import repository.TransactionRepository;

import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SHpcApplication {

	public static void main(String[] args) {
		SpringApplication.run(SHpcApplication.class, args);
	}

    @Bean
    public CommandLineRunner init(TransactionRepository transactionRepository) {
        return args -> {
            // Initialization logic can be added here if needed
            List<Long> accounts = List.of(1L, 2L, 3L, 4L, 5L);
            // For example, you could create some initial transactions for these accounts
            accounts.forEach(accountId -> {
                // Create and save initial transactions for each account)
                for (TransactionType transactionType : TransactionType.values()) {
                    for (int i = 0; i < 5; i++) {
                        Transaction transaction = Transaction.builder()
                                .accountId(accountId)
                                .amount(Math.random() * 1000)
                                .type(transactionType)
                                .status(TransactionStatus.PENDING)
                                .date(new Date())
                                .build();
                        transactionRepository.save(transaction);
                    }
                }
            });
        };
    }
}
