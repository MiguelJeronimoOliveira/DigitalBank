package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.DTOs.CardDTO;
import com.miguel.jeronimo.DigitalBank.Entities.Card;
import com.miguel.jeronimo.DigitalBank.Entities.CardStatement;
import com.miguel.jeronimo.DigitalBank.Entities.Transaction;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Exceptions.CardNotFoundException;
import com.miguel.jeronimo.DigitalBank.Exceptions.InsufficientBalanceException;
import com.miguel.jeronimo.DigitalBank.Exceptions.InvalidPasswordException;
import com.miguel.jeronimo.DigitalBank.Exceptions.UserNotFoundException;
import com.miguel.jeronimo.DigitalBank.Repositories.CardRepository;
import com.miguel.jeronimo.DigitalBank.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private final CardRepository repository;
    private final TransactionAuxService auxService;

    public CardService(CardRepository repository, UserRepository userRepository, TransactionService auxService, TransactionAuxService auxService1) {
        this.repository = repository;
        this.auxService = auxService1;
    }

    public Card DTOToEntity(CardDTO cardDTO) {
        Card card = new Card();

        User user = auxService.findUserById(cardDTO.user().getId())
                .orElseThrow(() -> new UserNotFoundException(""));

        card.setUser(user);
        card.setCardHolderName(user.getName());
        card.setCardPassword(cardDTO.cardPassword());
        card.setCredit(cardDTO.credit());
        card.setExpMonth(LocalDate.now().getMonthValue());
        card.setExpYear(LocalDate.now().getYear() + 10);

        if(card.isCredit()){
            card.setTotalLimit(BigDecimal.valueOf(700));
            card.setCardLimit(card.getTotalLimit());
            card.setDueDate(cardDTO.dueDate());
        }

        return card;
    }

    public void createCard(CardDTO request) {
        Card card = DTOToEntity(request);
        repository.save(card);
    }

    public void payActiveInstallment(CardDTO request) {
        User user = auxService.findUserById(request.user().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<CardStatement> activeStatementOptional = auxService.findCardStatmentActiveByUserCard(user.getCard());

        if (activeStatementOptional.isPresent()) {
            CardStatement activeStatement = activeStatementOptional.get();

            validateBalance(user, activeStatement);

            BigDecimal statementValue = activeStatement.getValue();

            user.setBalance(user.getBalance().subtract(statementValue));
            activeStatement.setValue(BigDecimal.ZERO);
            user.getCard().setCardLimit(user.getCard().getCardLimit().add(statementValue));

            auxService.save(user);
        }
    }

    public void payInstallments(CardDTO request, BigDecimal value) {
        User user = auxService.findUserById(request.user().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        int i = 0;
        while (value.compareTo(BigDecimal.ZERO) > 0) {
            LocalDate dueDate = LocalDate.now().plusMonths(i);
            int month = dueDate.getMonthValue();
            int year = dueDate.getYear();

            Optional<CardStatement> statementOptional = user.getCard().getCardStatement()
                    .stream()
                    .filter(cs -> cs.getMonth() == month && cs.getYear() == year && cs.getCard().getUser().equals(user))
                    .findFirst();

            if (statementOptional.isPresent()) {
                CardStatement statement = statementOptional.get();
                BigDecimal currentStatementValue = statement.getValue();

                if (value.compareTo(currentStatementValue) >= 0) {
                    value = value.subtract(currentStatementValue);
                    user.setBalance(user.getBalance().subtract(currentStatementValue));
                    statement.setValue(BigDecimal.ZERO);
                    user.getCard().setCardLimit(user.getCard().getCardLimit().add(currentStatementValue));
                } else {
                    statement.setValue(currentStatementValue.subtract(value));
                    user.setBalance(user.getBalance().subtract(value));
                    user.getCard().setCardLimit(user.getCard().getCardLimit().add(value));
                    value = BigDecimal.ZERO;
                }
            } else {
                break;
            }
            i++;
        }

        auxService.save(user);
    }

    public void deleteCard(Long cardId, String cardPassword) {
       Card card = repository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(""));

       if(!card.getCardPassword().equals(cardPassword))
           throw new InvalidPasswordException("");

       repository.delete(card);
    }

    public void desactivateCard(Long cardId) {
        Card card = repository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(""));

        int actualMonth = LocalDateTime.now().getMonthValue();
        int actualYear = LocalDateTime.now().getYear();

        if(card.getExpMonth() <= actualMonth && card.getExpYear() <= actualYear){
            card.setExpired(true);
        }
    }

    public List<Card> getAllCards() {
        return repository.findAll();
    }

    public Card getUserCard(Long userId) {
        User user = auxService.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return user.getCard();
    }

    private void validateBalance(User user, CardStatement statement) {
        if(user.getBalance().compareTo(statement.getValue()) < 0)
            throw new InsufficientBalanceException("Insufficient balance");
    }

}
