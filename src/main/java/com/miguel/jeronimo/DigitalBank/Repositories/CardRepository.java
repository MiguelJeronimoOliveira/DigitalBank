package com.miguel.jeronimo.DigitalBank.Repositories;

import com.miguel.jeronimo.DigitalBank.Entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
