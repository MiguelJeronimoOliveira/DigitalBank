package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.Entities.Card;
import com.miguel.jeronimo.DigitalBank.Entities.CardStatement;
import com.miguel.jeronimo.DigitalBank.Entities.Transaction;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Repositories.CardRepository;
import com.miguel.jeronimo.DigitalBank.Repositories.CardStatementRepository;
import com.miguel.jeronimo.DigitalBank.Repositories.TransactionRepository;
import com.miguel.jeronimo.DigitalBank.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TransactionAuxService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CardStatementRepository statementRepository;
    private final CardRepository cardRepository;

    public TransactionAuxService(TransactionRepository repository, TransactionRepository transactionRepository, TransactionProducer producer, UserRepository userRepository, CardStatementRepository statementRepository, CardRepository cardRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.statementRepository = statementRepository;
        this.cardRepository = cardRepository;
    }

    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findUserByPixKey(String pixKey) {
        return userRepository.findByPixKey(pixKey);
    }

    public Optional<CardStatement> findCardStatmentActiveByUserCard(Card card) {
        return statementRepository.findCardStatmentActiveByUserCard(card);
    }

    public Optional<Card>findCardById(Long id){
        return cardRepository.findCardById(id);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void save(Card card){
        cardRepository.save(card);
    }

    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public void updateBalances(User sender, User receiver) {
        save(sender);
        save(receiver);
    }

    public void updateBalance(User sender){
        save(sender);
    }
}
