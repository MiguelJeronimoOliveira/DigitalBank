package com.miguel.jeronimo.DigitalBank.Services;

import com.miguel.jeronimo.DigitalBank.DTOs.RegisterRequestDTO;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Enums.PixKeyType;
import com.miguel.jeronimo.DigitalBank.Exceptions.ArgumentAlreadyExistsException;
import com.miguel.jeronimo.DigitalBank.Repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    private User DTOToUser(RegisterRequestDTO request, String encryptedPassword) {
        User user = new User();

        user.setPerson(request.person());

        if(user.isPerson()) {
            user.setCpf(request.login());
            user.setLogin(request.login());
        }

        user.setName(request.name());
        user.setCnpj(request.login());
        user.setLogin(request.login());
        user.setEmail(request.email());
        user.setPassword(encryptedPassword);
        user.setPixKeyType(request.pixKeyType());

        setPixKey(user);

        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByLogin(username);
    }

    public void register(RegisterRequestDTO registerRequestDTO, String encryptedPassword) throws ArgumentAlreadyExistsException {
        User user = DTOToUser(registerRequestDTO, encryptedPassword);

        validateUser(user);

        repository.save(user);
    }




    private void setPixKey(User user){
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

    private void validateUser(User user) throws ArgumentAlreadyExistsException {

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
