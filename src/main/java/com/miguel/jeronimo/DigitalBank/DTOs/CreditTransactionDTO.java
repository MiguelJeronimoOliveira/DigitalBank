package com.miguel.jeronimo.DigitalBank.DTOs;

import com.miguel.jeronimo.DigitalBank.Entities.User;

import java.math.BigDecimal;

public record CreditTransactionDTO(BigDecimal amount, int installmentsNumber) {
}
