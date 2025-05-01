package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.DTOs.CreditTransactionDTO;
import com.miguel.jeronimo.DigitalBank.Entities.Card;
import com.miguel.jeronimo.DigitalBank.Entities.CardStatement;
import com.miguel.jeronimo.DigitalBank.Entities.Transaction;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Enums.TransactionStatus;
import com.miguel.jeronimo.DigitalBank.Enums.TransactionType;
import com.miguel.jeronimo.DigitalBank.Exceptions.UserNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class TransactionConsumer {

    private final TransactionAuxService service;

    public TransactionConsumer(TransactionAuxService service) {
        this.service = service;
    }

    @KafkaListener(topics = "processPix-topic", concurrency = "2", groupId = "transaction-group")
    public void processTransaction(Transaction transaction) {

        transaction.getSender().setBalance(transaction.getSender().getBalance().subtract(transaction.getAmount()));
        transaction.getReceiver().setBalance(transaction.getReceiver().getBalance().add(transaction.getAmount()));

        service.updateBalances(transaction.getSender(), transaction.getReceiver());

        transaction.setStatus(TransactionStatus.COMPLETED);
        service.save(transaction);

        System.out.println("transaction processed successfully");
    }

    @KafkaListener(topics = "processDebit-topic", groupId = "transaction-group")
    public void processDebitTransaction(Transaction transaction) {
        transaction.getSender().setBalance(transaction.getSender().getBalance().subtract(transaction.getAmount()));

        transaction.setStatus(TransactionStatus.COMPLETED);
        service.updateBalance(transaction.getSender());

        System.out.println("transaction processed successfully");
    }

    @KafkaListener(topics = "refund-topic", concurrency = "2", groupId = "transaction-group")
    public void refundTransaction(Transaction transaction) {

        transaction.getSender().setBalance(transaction.getSender().getBalance().add(transaction.getAmount()));
        transaction.getReceiver().setBalance(transaction.getReceiver().getBalance().subtract(transaction.getAmount()));

        service.updateBalances(transaction.getSender(), transaction.getReceiver());

        transaction.setStatus(TransactionStatus.REFUNDED);
        service.save(transaction);
        
        System.out.println("transaction refunded successfully");
    }
}
