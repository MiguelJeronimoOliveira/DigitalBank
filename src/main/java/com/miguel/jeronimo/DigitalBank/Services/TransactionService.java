package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.DTOs.CreditTransactionDTO;
import com.miguel.jeronimo.DigitalBank.DTOs.DebitTransactionDTO;
import com.miguel.jeronimo.DigitalBank.DTOs.TransactionDTO;
import com.miguel.jeronimo.DigitalBank.Entities.Card;
import com.miguel.jeronimo.DigitalBank.Entities.CardStatement;
import com.miguel.jeronimo.DigitalBank.Entities.Transaction;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Enums.TransactionStatus;
import com.miguel.jeronimo.DigitalBank.Enums.TransactionType;
import com.miguel.jeronimo.DigitalBank.Exceptions.InsufficientBalanceException;
import com.miguel.jeronimo.DigitalBank.Exceptions.InvalidCardException;
import com.miguel.jeronimo.DigitalBank.Exceptions.UserNotFoundException;
import com.miguel.jeronimo.DigitalBank.Repositories.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository repository;
    private TransactionAuxService auxService;
    private final TransactionProducer producer;

    public TransactionService(TransactionRepository repository, TransactionAuxService auxService, TransactionProducer producer) {
        this.repository = repository;
        this.auxService = auxService;
        this.producer = producer;
    }

    public Transaction DTOToPixEntity(TransactionDTO request) {
        Transaction transaction = new Transaction();

        User sender = auxService.findUserById(auxService.getLoggedUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User receiver = auxService.findUserByPixKey(request.receiver().getPixKey())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.amount());
        transaction.setInstallmentsNumber(0);
        transaction.setType(TransactionType.PIX);

        return transaction;
    }

    public Transaction DTOtoDebitEntity(DebitTransactionDTO request) {
        User user = auxService.findUserById(auxService.getLoggedUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.DEBIT);
        transaction.setInstallmentsNumber(0);
        transaction.setAmount(request.amount());
        transaction.setSender(user);
        transaction.setReceiver(null);

        return transaction;
    }

    public void createPixTransaction(TransactionDTO request) throws InsufficientBalanceException {
        Transaction transaction = DTOToPixEntity(request);

        Long id = auxService.getLoggedUserId();

        try {

            validateTransaction(transaction);

            transaction.setType(TransactionType.PIX);
            transaction.setStatus(TransactionStatus.PROCESSING);

            producer.send("processTransaction-topic", transaction);


        }catch (InsufficientBalanceException | IllegalArgumentException e) {
            transaction.setStatus(TransactionStatus.FAILED);
            repository.save(transaction);
            log.error(e.getMessage());
        }
    }

    public void createDebitTransaction(DebitTransactionDTO request) throws InsufficientBalanceException {
        Transaction transaction = DTOtoDebitEntity(request);

        transaction.setStatus(TransactionStatus.PROCESSING);

        producer.send("processDebit-topic", transaction);
    }

    public void createCreditTransaction(CreditTransactionDTO creditTransaction, User user) {
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.CREDIT);
        transaction.setInstallmentsNumber(creditTransaction.installmentsNumber());
        transaction.setAmount(creditTransaction.amount());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setSender(user);
        transaction.setReceiver(null);

        repository.save(transaction);
    }

    public void updateInstallment(CreditTransactionDTO transaction) {

        User user = auxService.findUserById(auxService.getLoggedUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Card card = user.getCard();

        validateCard(card);
        validateCredit(card, transaction);

        BigDecimal installmentsNumber = BigDecimal.valueOf(transaction.installmentsNumber());
        BigDecimal installmentValue = transaction.amount().divide(installmentsNumber, 2, RoundingMode.HALF_UP);

        LocalDate today = LocalDate.now();
        Optional<CardStatement> activeStatement = auxService.findCardStatmentActiveByUserCard(card);

        card.setCardLimit(card.getCardLimit().subtract(transaction.amount()));

        createCreditTransaction(transaction,user);

        int startIndex = 0;

        if (activeStatement.isPresent() && card.getDueDate() >= today.getDayOfMonth()) {
            CardStatement currentStatement = activeStatement.get();
            currentStatement.setValue(currentStatement.getValue().add(installmentValue));
            currentStatement.setTotalValue(currentStatement.getTotalValue().add(installmentValue));
            startIndex = 1;
        }

        for (int i = startIndex; i < transaction.installmentsNumber(); i++) {
            LocalDate dueDate = today.plusMonths(i);
            int month = dueDate.getMonthValue();
            int year = dueDate.getYear();

            Optional<CardStatement> existingStatement = card.getCardStatement()
                    .stream()
                    .filter(cs -> cs.getMonth() == month && cs.getYear() == year)
                    .findFirst();

            if (existingStatement.isPresent()) {
                CardStatement statement = existingStatement.get();
                statement.setValue(statement.getValue().add(installmentValue));
                statement.setTotalValue(statement.getTotalValue().add(installmentValue));
            } else {
                CardStatement newStatement = new CardStatement();
                newStatement.setCard(card);
                newStatement.setMonth(month);
                newStatement.setYear(year);
                newStatement.setValue(installmentValue);
                newStatement.setTotalValue(installmentValue);
                newStatement.setActive(i == 0);
                card.getCardStatement().add(newStatement);
            }
        }

        auxService.save(card);
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

    private void validateTransaction(Transaction transaction){

        validateBalance(transaction);
        validateReceiver(transaction);
    }

    private void validateBalance(Transaction transaction){
        if(transaction.getSender().getBalance().compareTo(transaction.getAmount()) < 0)
            throw new InsufficientBalanceException("Insufficient balance");
    }

    private void validateReceiver(Transaction transaction){
        if(!transaction.getReceiver().isActive())
            throw new IllegalArgumentException("User is not active");
    }

    private void validateCard(Card card){
        int actualMonth = LocalDateTime.now().getMonthValue();
        int actualYear = LocalDateTime.now().getYear();

        if((card.getExpMonth() <= actualMonth &&
                card.getExpYear() <= actualYear) ||
                card.isExpired()){

            throw new InvalidCardException("This card is expired");
        }

        if(!card.isCredit()){
            throw new InvalidCardException("This card is not credit");
        }
    }

    private void validateCredit(Card card, CreditTransactionDTO transaction ){
        if(card.getCardLimit().compareTo(transaction.amount()) < 0){
            throw new InsufficientBalanceException("Insufficient credit");
        }
    }
}
