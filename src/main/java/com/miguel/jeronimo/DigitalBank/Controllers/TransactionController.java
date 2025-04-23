package com.miguel.jeronimo.DigitalBank.Controllers;

import com.miguel.jeronimo.DigitalBank.DTOs.TransactionDTO;
import com.miguel.jeronimo.DigitalBank.Services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity postTransaction(@RequestBody TransactionDTO request) {
        service.createTransaction(request);
        return ResponseEntity.ok().build();
    }

}
