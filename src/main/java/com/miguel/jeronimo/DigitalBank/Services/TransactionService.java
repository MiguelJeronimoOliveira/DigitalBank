package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.DTOs.TransactionDTO;
import com.miguel.jeronimo.DigitalBank.Entities.Transaction;
import com.miguel.jeronimo.DigitalBank.Entities.TransactionStatus;
import com.miguel.jeronimo.DigitalBank.Repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionProducer producer;

    public TransactionService(TransactionRepository repository, TransactionProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public void createTransaction(TransactionDTO request) {
        Transaction transaction = new Transaction();

        try {
            transaction.setSender(request.sender());
            transaction.setReceiver(request.receiver());
            transaction.setAmount(request.amount());

            validateTransaction(request);
            transaction.setStatus(TransactionStatus.PROCESSING);
            repository.save(transaction);

            processTransaction(transaction);


        }catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            e.printStackTrace();
        }finally {
            repository.save(transaction);
        }
    }

    public void validateTransaction(TransactionDTO request) throws Exception {

        if(request.sender().getBalance().compareTo(request.amount()) < 0)
            throw new Exception();

        if(!request.receiver().isActive())
            throw new Exception();
    }


    public void processTransaction(Transaction transaction) {
        transaction.getSender().setBalance(transaction.getSender().getBalance().subtract(transaction.getAmount()));
        transaction.getReceiver().setBalance(transaction.getReceiver().getBalance().add(transaction.getAmount()));
        transaction.setStatus(TransactionStatus.COMPLETED);
    }

    public void refundTransaction(Long id) {
        Optional<Transaction> transactionOptional = repository.findById(id);

        LocalDateTime actualTime = LocalDateTime.now();

        if(transactionOptional.isPresent() &&
        transactionOptional.get().getDate().isBefore(actualTime.plusMinutes(10))) {
            
            Transaction transaction = transactionOptional.get();

            transaction.getSender().setBalance(transaction.getSender().getBalance().add(transaction.getAmount()));
            transaction.getReceiver().setBalance(transaction.getReceiver().getBalance().subtract(transaction.getAmount()));

            transaction.setStatus(TransactionStatus.REFUNDED);
            repository.save(transaction);
        }
    }
    public void cancelTransaction(Long id) {
        Optional<Transaction> transactionOptional = repository.findById(id);

        if(transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();
            transaction.setStatus(TransactionStatus.CANCELLED);
        }
    }


}
