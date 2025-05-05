package com.miguel.jeronimo.DigitalBank.DTOs;

import com.miguel.jeronimo.DigitalBank.Enums.PixKeyType;

import java.time.LocalDate;

public record RegisterRequestDTO(String login,String name, String email, String phone,
                                 LocalDate bornDate, boolean person, PixKeyType pixKeyType, String password) {
}
