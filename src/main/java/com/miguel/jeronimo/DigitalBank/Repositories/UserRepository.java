package com.miguel.jeronimo.DigitalBank.Repositories;

import com.miguel.jeronimo.DigitalBank.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.cpf = ?1 AND u.active = true")
    Optional<User> findByCpf(String cpf);

    @Query("SELECT u FROM User u WHERE u.pixKey = ?1 AND u.active = true")
    Optional<User> findByPixKey(String pixKey);

    @Query("SELECT u FROM User u WHERE u.cnpj = ?1 AND u.active = true")
    Optional<User> findByCnpj(String cnpj);
}
