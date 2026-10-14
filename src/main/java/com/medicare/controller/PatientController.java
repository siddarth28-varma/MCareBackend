package com.medicare.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import com.medicare.model.Patient;
import com.medicare.repository.PatientRepository;
import com.medicare.repository.UserRepository;
import com.medicare.security.AuthorizationService;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientRepository patients;
    private final UserRepository users;
    private final AuthorizationService authorization;
    public PatientController(PatientRepository patients, UserRepository users, AuthorizationService authorization) {
        this.patients = patients;
        this.users = users;
        this.authorization = authorization;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Patient> all() { return patients.findAll(); }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @authorization.ownsUser(#p0, authentication)")
    public Patient byUser(@PathVariable Long userId) { return patients.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found.")); }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public Patient currentPatient(Authentication authentication) {
        return authorization.currentPatient(authentication)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authorization.ownsPatient(#p0, authentication)")
    public Patient one(@PathVariable Long id) { return patients.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found.")); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authorization.ownsPatient(#p0, authentication)")
    public Patient update(@PathVariable Long id, @RequestBody Patient request) {
        Patient patient = patients.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found."));
        patient.getUser().setName(request.getUser().getName());
        users.save(patient.getUser());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        return patients.save(patient);
    }
}
