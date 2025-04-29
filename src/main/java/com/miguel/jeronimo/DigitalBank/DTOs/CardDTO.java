package com.miguel.jeronimo.DigitalBank.DTOs;

import com.miguel.jeronimo.DigitalBank.Entities.User;

public record CardDTO(User user, boolean credit, int dueDate, String cardPassword) {
}
