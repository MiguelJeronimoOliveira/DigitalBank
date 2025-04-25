package com.miguel.jeronimo.DigitalBank.Controllers;

import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Exceptions.ArgumentAlreadyExistsException;
import com.miguel.jeronimo.DigitalBank.Services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity postUser(@RequestBody User user) throws ArgumentAlreadyExistsException {
        service.createUser(user);
        return ResponseEntity.ok().build();
    }
}
