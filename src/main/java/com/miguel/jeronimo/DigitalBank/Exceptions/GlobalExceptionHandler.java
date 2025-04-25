package com.miguel.jeronimo.DigitalBank.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleGlobalException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Error " + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity handleIllegalArgumentException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Parameter " + ex.getMessage());
    }

    @ExceptionHandler(ArgumentAlreadyExistsException.class)
    public ResponseEntity handleArgumentAlreadyExists(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parameter Already  " + ex.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity handleInsufficientBalance(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient Balance " + ex.getMessage());
    }

    @ExceptionHandler(InvalidPaswwrodException.class)
    public ResponseEntity handleInvalidPaswwrod(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Paswwrod " + ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity handleUserNotFound(Exception ex, WebRequest request) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found " + ex.getMessage());
    }
}
