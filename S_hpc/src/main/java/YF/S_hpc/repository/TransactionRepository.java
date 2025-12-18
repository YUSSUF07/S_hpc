package YF.S_hpc.repository;

import YF.S_hpc.entities.Transaction;
import YF.S_hpc.entities.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findByTransactionStatus(TransactionStatus status);
}
