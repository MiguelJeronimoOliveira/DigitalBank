package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.DTOs.CardDTO;
import com.miguel.jeronimo.DigitalBank.DTOs.PayInstallmentsDTO;
import com.miguel.jeronimo.DigitalBank.Entities.Card;
import com.miguel.jeronimo.DigitalBank.Entities.CardStatement;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Exceptions.CardNotFoundException;
import com.miguel.jeronimo.DigitalBank.Exceptions.InsufficientBalanceException;
import com.miguel.jeronimo.DigitalBank.Exceptions.InvalidPasswordException;
import com.miguel.jeronimo.DigitalBank.Exceptions.UserNotFoundException;
import com.miguel.jeronimo.DigitalBank.Repositories.CardRepository;
import com.miguel.jeronimo.DigitalBank.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
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

    public CardService(CardRepository repository, TransactionAuxService auxService) {
        this.repository = repository;
        this.auxService = auxService;
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

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void autodebit() {
        System.out.println("iniciando timer");

        User user = auxService.findUserById(auxService.getLoggedUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Card card = auxService.findCardById(user.getCard().getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        Optional<CardStatement> activeStatementOptional = auxService.findCardStatmentActiveByUserCard(user.getCard());

        if (activeStatementOptional.isPresent() && card.isAutoDebit()) {
            CardStatement activeStatement = activeStatementOptional.get();

            LocalDate statementDueDate = LocalDate.of(activeStatement.getYear(), activeStatement.getMonth(), user.getCard().getDueDate());
            boolean isPastDueOrToday = !statementDueDate.isAfter(LocalDate.now());

            if (isPastDueOrToday && activeStatement.isActive()) {
                activeStatement.setActive(false);
                activeStatement.setPaid(true);

                int nextMonth = statementDueDate.plusMonths(1).getMonthValue();
                int nextYear = statementDueDate.plusMonths(1).getYear();

                card.getCardStatement()
                        .stream()
                        .filter(cs -> cs.getMonth() == nextMonth && cs.getYear() == nextYear)
                        .findFirst()
                        .ifPresent(next -> next.setActive(true));

                validateBalance(user, activeStatement);

                BigDecimal statementValue = activeStatement.getValue();

                user.setBalance(user.getBalance().subtract(statementValue));
                activeStatement.setValue(BigDecimal.ZERO);
                user.getCard().setCardLimit(
                        user.getCard().getCardLimit().add(statementValue)
                );

                auxService.save(user);
            }
        }
    }

    public void payActiveInstallment(CardDTO request) {
        User user = auxService.findUserById(auxService.getLoggedUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<CardStatement> activeStatementOptional = auxService.findCardStatmentActiveByUserCard(user.getCard());

        if (activeStatementOptional.isPresent()) {
            CardStatement activeStatement = activeStatementOptional.get();

            LocalDate statementDueDate = LocalDate.of(activeStatement.getYear(), activeStatement.getMonth(), user.getCard().getDueDate());
            boolean isPastDueOrToday = !statementDueDate.isAfter(LocalDate.now());

            if (isPastDueOrToday && activeStatement.isActive()) {
                activeStatement.setActive(false);
                activeStatement.setPaid(true);


                int nextMonth = statementDueDate.plusMonths(1).getMonthValue();
                int nextYear = statementDueDate.plusMonths(1).getYear();

                user.getCard().getCardStatement()
                        .stream()
                        .filter(cs -> cs.getMonth() == nextMonth && cs.getYear() == nextYear)
                        .findFirst()
                        .ifPresent(next -> next.setActive(true));


                validateBalance(user, activeStatement);

                BigDecimal statementValue = activeStatement.getValue();

                user.setBalance(user.getBalance().subtract(statementValue));
                activeStatement.setValue(BigDecimal.ZERO);
                user.getCard().setCardLimit(user.getCard().getCardLimit().add(statementValue));

                auxService.save(user);
            }
        }
    }

    public void payInstallments(PayInstallmentsDTO request) {
        User user = auxService.findUserById(auxService.getLoggedUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BigDecimal remainingValue = request.value();
        int offset = 0;

        while (remainingValue.compareTo(BigDecimal.ZERO) > 0) {
            LocalDate futureDate = LocalDate.now().plusMonths(offset);
            int month = futureDate.getMonthValue();
            int year = futureDate.getYear();

            Optional<CardStatement> optionalStatement = user.getCard().getCardStatement()
                    .stream()
                    .filter(cs -> cs.getMonth() == month && cs.getYear() == year)
                    .findFirst();

            if (optionalStatement.isEmpty()) break;

            CardStatement statement = optionalStatement.get();

            LocalDate dueDate = LocalDate.of(statement.getYear(), statement.getMonth(), user.getCard().getDueDate());
            boolean isPastDueOrToday = !dueDate.isAfter(LocalDate.now());

            BigDecimal statementValue = statement.getValue();

            if (statementValue.compareTo(BigDecimal.ZERO) == 0) {
                offset++;
                continue;
            }

            if (remainingValue.compareTo(statementValue) >= 0) {
                remainingValue = remainingValue.subtract(statementValue);
                user.setBalance(user.getBalance().subtract(statementValue));
                user.getCard().setCardLimit(user.getCard().getCardLimit().add(statementValue));

                statement.setValue(BigDecimal.ZERO);

                if (isPastDueOrToday && statement.isActive()) {
                    statement.setActive(false);
                    statement.setPaid(true);

                    LocalDate nextDate = futureDate.plusMonths(1);
                    user.getCard().getCardStatement().stream()
                            .filter(cs -> cs.getMonth() == nextDate.getMonthValue()
                                    && cs.getYear() == nextDate.getYear())
                            .findFirst()
                            .ifPresent(next -> next.setActive(true));
                }

            } else {
                statement.setValue(statementValue.subtract(remainingValue));
                user.setBalance(user.getBalance().subtract(remainingValue));
                user.getCard().setCardLimit(user.getCard().getCardLimit().add(remainingValue));
                remainingValue = BigDecimal.ZERO;
            }

            offset++;
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

    public Card getUserCard() {
        User user = auxService.findUserById(auxService.getLoggedUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return user.getCard();
    }

    private void validateBalance(User user, CardStatement statement) {
        if(user.getBalance().compareTo(statement.getValue()) < 0)
            throw new InsufficientBalanceException("Insufficient balance");
    }

}
