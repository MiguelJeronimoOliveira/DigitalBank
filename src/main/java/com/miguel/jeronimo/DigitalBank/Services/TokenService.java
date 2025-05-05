package com.miguel.jeronimo.DigitalBank.Services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {


    Instant expirationTime =  Instant.now().plusSeconds(3600);

    public String generateToken(User user){

        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");

            return JWT.create()
                    .withSubject(user.getLogin())
                    .withIssuer("digitalBank")
                    .withExpiresAt(expirationTime)
                    .sign(algorithm);
        }catch (JWTCreationException exception){
            throw new RuntimeException(exception);
        }

    }

    public String validateToken(String token){

        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            return JWT.require(algorithm)
                    .withIssuer("digitalBank")
                    .build()
                    .verify(token)
                    .getSubject();
        }catch (JWTCreationException exception){
            return "";
        }
    }


}
