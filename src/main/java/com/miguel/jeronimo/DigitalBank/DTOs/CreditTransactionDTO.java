package com.miguel.jeronimo.DigitalBank.DTOs;

import com.miguel.jeronimo.DigitalBank.Entities.User;

import java.math.BigDecimal;

public record CreditTransactionDTO(User sender, BigDecimal amount, int installmentsNumber) {
}
