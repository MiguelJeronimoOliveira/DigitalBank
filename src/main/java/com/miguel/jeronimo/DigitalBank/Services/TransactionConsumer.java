package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.Entities.Transaction;
import com.miguel.jeronimo.DigitalBank.Enums.TransactionStatus;
import com.miguel.jeronimo.DigitalBank.Repositories.TransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

    private final TransactionService transactionService;

    public TransactionConsumer(TransactionService transactionService, TransactionRepository transactionRepository) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "process-topic", concurrency = "2", groupId = "transaction-group")
    public void processTransaction(Transaction transaction) {

        transaction.getSender().setBalance(transaction.getSender().getBalance().subtract(transaction.getAmount()));
        transaction.getReceiver().setBalance(transaction.getReceiver().getBalance().add(transaction.getAmount()));

        transactionService.updateBalances(transaction.getSender(), transaction.getReceiver());

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionService.save(transaction);

        System.out.println("transaction processed successfully");
    }

    @KafkaListener(topics = "refund-topic", concurrency = "2", groupId = "transaction-group")
    public void refundTransaction(Transaction transaction) {

        transaction.getSender().setBalance(transaction.getSender().getBalance().add(transaction.getAmount()));
        transaction.getReceiver().setBalance(transaction.getReceiver().getBalance().subtract(transaction.getAmount()));

        transactionService.updateBalances(transaction.getSender(), transaction.getReceiver());

        transaction.setStatus(TransactionStatus.REFUNDED);
        transactionService.save(transaction);
        
        System.out.println("transaction refunded successfully");
    }
}
