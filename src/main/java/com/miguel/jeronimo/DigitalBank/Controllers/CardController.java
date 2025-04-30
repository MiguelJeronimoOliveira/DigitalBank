package com.miguel.jeronimo.DigitalBank.Controllers;

import com.miguel.jeronimo.DigitalBank.DTOs.CardDTO;
import com.miguel.jeronimo.DigitalBank.DTOs.CardResponseDTO;
import com.miguel.jeronimo.DigitalBank.DTOs.DeleteRequestDTO;
import com.miguel.jeronimo.DigitalBank.DTOs.PayInstallmentsDTO;
import com.miguel.jeronimo.DigitalBank.Entities.Card;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Services.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/card")
public class CardController {

    private final CardService service;

    public CardController(CardService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity createCard(@RequestBody CardDTO request) {
        service.createCard(request);
        return ResponseEntity.ok("Card created");
    }

    @DeleteMapping
    public ResponseEntity deleteCard(@RequestBody DeleteRequestDTO request) {
        service.deleteCard(request.id(), request.password());
        return ResponseEntity.ok("Card deleted");
    }

    @GetMapping
    public ResponseEntity<List<CardResponseDTO>> getAllCards() {
        List<Card> cards = service.getAllCards();
        List<CardResponseDTO> response = cards.stream()
                .map(CardResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity getUserCard(@PathVariable Long userId) {
        Card card = service.getUserCard(userId);
        return ResponseEntity.ok(CardResponseDTO.fromEntity(card));
    }

    @PostMapping("/pay")
    public ResponseEntity payInstallment(@RequestBody CardDTO request) {
        service.payActiveInstallment(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payValue")
    public ResponseEntity payInstallments(@RequestBody PayInstallmentsDTO request) {
        service.payInstallments(request);
        return ResponseEntity.ok().build();
    }
}
