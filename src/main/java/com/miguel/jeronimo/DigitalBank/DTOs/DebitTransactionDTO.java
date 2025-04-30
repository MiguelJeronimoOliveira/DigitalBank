package com.miguel.jeronimo.DigitalBank.DTOs;

import com.miguel.jeronimo.DigitalBank.Entities.User;

import java.math.BigDecimal;

public record DebitTransactionDTO(User sender, BigDecimal amount) {
}
