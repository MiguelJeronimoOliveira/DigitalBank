package com.miguel.jeronimo.DigitalBank.Controllers;

import com.miguel.jeronimo.DigitalBank.DTOs.DeleteRequestDTO;
import com.miguel.jeronimo.DigitalBank.Entities.User;
import com.miguel.jeronimo.DigitalBank.Exceptions.ArgumentAlreadyExistsException;
import com.miguel.jeronimo.DigitalBank.Services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok("User created");
    }

    @PostMapping("/desactive")
    public ResponseEntity desactivateUser (@RequestBody DeleteRequestDTO request) {
        service.desactivateUser(request.id(), request.password());
        return ResponseEntity.ok("User desactivated");
    }

    @PostMapping("/delete")
    public ResponseEntity deleteUser(@RequestBody DeleteRequestDTO request) {
        service.deleteUser(request.id(), request.password());
        return ResponseEntity.ok("User deleted");
    }
}
