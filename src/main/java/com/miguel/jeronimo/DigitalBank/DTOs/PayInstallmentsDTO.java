package com.miguel.jeronimo.DigitalBank.DTOs;

import java.math.BigDecimal;

public record PayInstallmentsDTO(Long userId, BigDecimal value) {
}
