package com.miguel.jeronimo.DigitalBank.DTOs;

import com.miguel.jeronimo.DigitalBank.Enums.TransactionStatus;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Enums.TransactionType;

import java.math.BigDecimal;

public record TransactionDTO(User sender, User receiver, BigDecimal amount,
                             TransactionStatus status, TransactionType type, int installmentsNumber) {
}
