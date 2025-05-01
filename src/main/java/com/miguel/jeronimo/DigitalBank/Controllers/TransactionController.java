package com.miguel.jeronimo.DigitalBank.Controllers;

import com.miguel.jeronimo.DigitalBank.DTOs.CreditTransactionDTO;
import com.miguel.jeronimo.DigitalBank.DTOs.DebitTransactionDTO;
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
    public ResponseEntity postTransaction(@RequestBody TransactionDTO request) throws Exception {
        service.createPixTransaction(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/credit")
    public ResponseEntity postCreditTransaction(@RequestBody CreditTransactionDTO request) throws Exception {
        service.updateInstallment(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/debit")
    public ResponseEntity ResponseEntitypostDebitTransaction(@RequestBody DebitTransactionDTO request){
        service.createDebitTransaction(request);
        return ResponseEntity.ok().build();
    }

}
