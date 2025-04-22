package com.miguel.jeronimo.DigitalBank.DTOs;

import com.miguel.jeronimo.DigitalBank.Entities.TransactionStatus;
import com.miguel.jeronimo.DigitalBank.Entities.User;

import java.math.BigDecimal;

public record TransactionDTO(User sender, User receiver, BigDecimal amount, TransactionStatus status) {
}
