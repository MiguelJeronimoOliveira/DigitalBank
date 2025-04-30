package com.miguel.jeronimo.DigitalBank.DTOs;

import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Enums.TransactionType;

import java.math.BigDecimal;

public record CreditTransactionDTO(User sender, TransactionType type, BigDecimal amount, int installmentsNumber) {
}
