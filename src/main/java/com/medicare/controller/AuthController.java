package com.medicare.controller;

import com.medicare.dto.LoginRequest;
import com.medicare.dto.RegisterRequest;
import com.medicare.model.*;
import com.medicare.repository.PatientRepository;
import com.medicare.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PatientRepository patients;

    public AuthController(UserRepository users, PatientRepository patients) {
        this.users = users;
        this.patients = patients;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@Valid @RequestBody RegisterRequest request) {
        if (users.existsByEmail(request.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered.");
        User user = users.save(new User(request.name(), request.email(), request.password(), Role.PATIENT));
        patients.save(new Patient(user, request.phone(), request.dateOfBirth(), request.gender()));
        return user;
    }

    @PostMapping("/login")
    public User login(@Valid @RequestBody LoginRequest request) {
        User user = users.findByEmail(request.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password."));
        if (!user.getPassword().equals(request.password())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password.");
        return user;
    }
}
