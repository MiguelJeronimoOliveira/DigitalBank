package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.Enums.PixKeyType;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Exceptions.ArgumentAlreadyExistsException;
import com.miguel.jeronimo.DigitalBank.Exceptions.InvalidPasswordException;
import com.miguel.jeronimo.DigitalBank.Exceptions.UserNotFoundException;
import com.miguel.jeronimo.DigitalBank.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void createUser(User user) throws ArgumentAlreadyExistsException {

        try{
            validateUser(user);

            setPixKey(user);
            repository.save(user);

        }catch (ArgumentAlreadyExistsException | IllegalArgumentException e){
            log.error(e.getMessage());
            throw e;
        }

    }

    public void deleteUser(Long userId, String userPassword) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        if (!user.getPassword().equals(userPassword)) {
            throw new InvalidPasswordException("");
        }

        repository.deleteById(userId);
    }

    public List<User> getUsers() {
        return repository.findAll();
    }

    public void desactivateUser(Long userId, String userPassword) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        if (!user.getPassword().equals(userPassword)) {
            throw new InvalidPasswordException("");
        }

        user.setActive(false);
        repository.save(user);
    }

    public User findUser(String pixKey){
        Optional<User> user = repository.findByPixKey(pixKey);
        return user.orElse(null);
    }

    public BigDecimal getUserBalance(Long userId){
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(""));

        return user.getBalance();
    }

    public void setPixKey(User user){
        switch (user.getPixKeyType()){
            case PixKeyType.CPF:
                user.setPixKey(user.getCpf());
                break;
            case PixKeyType.EMAIL:
                user.setPixKey(user.getEmail());
                break;
            case PixKeyType.RANDOM_KEY:
                user.setPixKey(UUID.randomUUID().toString());
                break;
        }
    }

    public void validateUser(User user) throws ArgumentAlreadyExistsException {

        if(repository.findByCpf(user.getCpf()).isPresent())
            throw new ArgumentAlreadyExistsException("CPF already exists");

        if(repository.findByCnpj(user.getCnpj()).isPresent())
            throw new ArgumentAlreadyExistsException("CNPJ already exists");

        if(!user.isPerson()){
            if(!validateCNPJ(user))
                throw new IllegalArgumentException("CNPJ is invalid");
        }else{
            if(!validateCPF(user))
                throw new IllegalArgumentException("CPF is invalid");
        }

    }

    private boolean validateCPF(User user){
        if (user.getCpf().length() != 11 || user.getCpf().matches("(\\d)\\1{10}")) return false;

        int sum = 0, weight = 10;
        for (int i = 0; i < 9; i++) sum += (user.getCpf().charAt(i) - '0') * weight--;
        int digit1 = (sum * 10) % 11;
        digit1 = (digit1 == 10) ? 0 : digit1;

        sum = 0; weight = 11;
        for (int i = 0; i < 10; i++) sum += (user.getCpf().charAt(i) - '0') * weight--;
        int digit2 = (sum * 10) % 11;
        digit2 = (digit2 == 10) ? 0 : digit2;

        return digit1 == (user.getCpf().charAt(9) - '0') && digit2 == (user.getCpf().charAt(10) - '0');
    }

    private boolean validateCNPJ(User user){
        if (user.getCnpj().length() != 14 || user.getCnpj().matches("(\\d)\\1{13}")) return false;

        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < 12; i++)
            sum += (user.getCnpj().charAt(i) - '0') * pesos1[i];

        int digit1 = sum % 11;
        digit1 = (digit1 < 2) ? 0 : 11 - digit1;

        sum = 0;
        for (int i = 0; i < 13; i++)
            sum += (user.getCnpj().charAt(i) - '0') * pesos2[i];

        int digit2 = sum % 11;
        digit2 = (digit2 < 2) ? 0 : 11 - digit2;

        return user.getCnpj().charAt(12) - '0' == digit1 && user.getCnpj().charAt(13) - '0' == digit2;
    }
}
