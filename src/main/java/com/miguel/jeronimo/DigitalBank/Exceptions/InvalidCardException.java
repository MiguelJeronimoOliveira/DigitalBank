package com.miguel.jeronimo.DigitalBank.Exceptions;

public class InvalidCardException extends RuntimeException {
    public InvalidCardException(String message) {
        super(message);
    }
}
