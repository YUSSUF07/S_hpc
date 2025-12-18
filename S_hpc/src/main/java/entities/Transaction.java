package entities;


import java.util.Date;

public class Transaction {

    private Long id;
    private Date date;
    private Double amount;
    private TransactionStatus status;
    private TransactionType type;
}
