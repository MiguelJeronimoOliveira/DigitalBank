package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.DTOs.TransactionDTO;
import com.miguel.jeronimo.DigitalBank.Entities.Transaction;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Enums.TransactionStatus;
import com.miguel.jeronimo.DigitalBank.Exceptions.InsufficientBalanceException;
import com.miguel.jeronimo.DigitalBank.Exceptions.UserNotFoundException;
import com.miguel.jeronimo.DigitalBank.Repositories.TransactionRepository;
import com.miguel.jeronimo.DigitalBank.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransactionService {

    private static Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository repository;
    private final TransactionProducer producer;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository repository, TransactionProducer producer, UserRepository userRepository) {
        this.repository = repository;
        this.producer = producer;
        this.userRepository = userRepository;
    }

    public Transaction DTOToEntity(TransactionDTO request) {
        Transaction transaction = new Transaction();

        User sender = userRepository.findById(request.sender().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User receiver = userRepository.findByPixKey(request.receiver().getPixKey())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.amount());

        return transaction;
    }

    public void createTransaction(TransactionDTO request) throws InsufficientBalanceException {
        Transaction transaction = DTOToEntity(request);

        try {

            validateTransaction(transaction);

            transaction.setStatus(TransactionStatus.PROCESSING);

            producer.send("process-topic", transaction);

        }catch (InsufficientBalanceException e) {
            transaction.setStatus(TransactionStatus.FAILED);
            log.error(e.getMessage());
            throw e;
        }catch (IllegalArgumentException e){
            log.error(e.getMessage());
            throw e;
        }finally {
            repository.save(transaction);
        }
    }

    public void validateTransaction(Transaction transaction){

        if(transaction.getSender().getBalance().compareTo(transaction.getAmount()) < 0)
            throw new InsufficientBalanceException("Insufficient balance");

        if(!transaction.getReceiver().isActive())
            throw new IllegalArgumentException("User is not active");
    }

    public void refundTransaction(Long id) {
        Optional<Transaction> transactionOptional = repository.findById(id);

        LocalDateTime actualTime = LocalDateTime.now();

        if(transactionOptional.isPresent() &&
        transactionOptional.get().getDate().isBefore(actualTime.plusMinutes(10))) {
            
            Transaction transaction = transactionOptional.get();
            producer.send("refund-topic", transaction);

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

    public void updateBalances(User sender, User receiver) {
        userRepository.save(sender);
        userRepository.save(receiver);
    }

    public void save(Transaction transaction) {
        repository.save(transaction);
    }

}
