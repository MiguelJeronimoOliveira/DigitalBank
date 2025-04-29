package com.miguel.jeronimo.DigitalBank.Repositories;

import com.miguel.jeronimo.DigitalBank.Entities.CardStatement;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CardStatementRepository extends JpaRepository<CardStatement, Long> {

    @Query("SELECT c FROM CardStatement c where c.active = true")
    Optional<CardStatement> findCardStatmentActiveByUser(User user);
}
