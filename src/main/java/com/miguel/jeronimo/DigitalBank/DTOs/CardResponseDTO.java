package com.miguel.jeronimo.DigitalBank.DTOs;

import com.miguel.jeronimo.DigitalBank.Entities.Card;
import com.miguel.jeronimo.DigitalBank.Entities.CardStatement;

import java.math.BigDecimal;
import java.util.List;

public record CardResponseDTO(Long id, int cardNumber, int expMonth, int expYear, boolean expired, int cvv, String cardHolderName,
                              String cardPassword, boolean credit, int dueDate, BigDecimal totalLimit,
                              BigDecimal cardLimit, Long user, List<CardStatement> cardStatement) {

    public static CardResponseDTO fromEntity(Card card) {
        return new CardResponseDTO(
                card.getId(),
                card.getNumber(),
                card.getExpMonth(),
                card.getExpYear(),
                card.isExpired(),
                card.getCvv(),
                card.getCardHolderName(),
                card.getCardPassword(),
                card.isCredit(),
                card.getDueDate(),
                card.getTotalLimit(),
                card.getCardLimit(),
                card.getUser().getId(),
                card.getCardStatement()
        );
    }
}
