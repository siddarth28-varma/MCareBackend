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

import com.medicare.model.Patient;
import com.medicare.repository.PatientRepository;
import com.medicare.repository.UserRepository;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientRepository patients;
    private final UserRepository users;
    public PatientController(PatientRepository patients, UserRepository users) { this.patients = patients; this.users = users; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Patient> all() { return patients.findAll(); }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @authorization.ownsUser(#root.args[0], authentication)")
    public Patient byUser(@PathVariable Long userId) { return patients.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found.")); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authorization.ownsPatient(#root.args[0], authentication)")
    public Patient one(@PathVariable Long id) { return patients.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found.")); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authorization.ownsPatient(#root.args[0], authentication)")
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
