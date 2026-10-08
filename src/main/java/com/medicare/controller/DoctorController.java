package com.medicare.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.medicare.dto.DoctorRequest;
import com.medicare.model.Doctor;
import com.medicare.model.Role;
import com.medicare.model.User;
import com.medicare.repository.AppointmentRepository;
import com.medicare.repository.DoctorRepository;
import com.medicare.repository.UserRepository;
import com.medicare.service.AppointmentService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorRepository doctors;
    private final UserRepository users;
    private final AppointmentRepository appointments;
    private final AppointmentService appointmentService;

    public DoctorController(DoctorRepository doctors, UserRepository users, AppointmentRepository appointments, AppointmentService appointmentService) {
        this.doctors = doctors;
        this.users = users;
        this.appointments = appointments;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Doctor> all(@RequestParam(required = false) String specialization) {
        return specialization == null || specialization.isBlank() ? doctors.findAll() : doctors.findBySpecializationContainingIgnoreCase(specialization);
    }

    @GetMapping("/{id}")
    public Doctor one(@PathVariable Long id) { return doctors.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found.")); }

    @GetMapping("/by-user/{userId}")
    public Doctor byUser(@PathVariable Long userId) { return doctors.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found.")); }

    @GetMapping("/{id}/availability")
    public List<java.time.LocalTime> availability(@PathVariable Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.availableSlots(id, date);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Doctor add(@RequestBody DoctorRequest request) {
        if (users.existsByEmail(request.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered.");
        User user = users.save(new User(request.name(), request.email(), request.password(), Role.DOCTOR));
        return doctors.save(toDoctor(user, request));
    }

    @PutMapping("/{id}")
    public Doctor update(@PathVariable Long id, @RequestBody DoctorRequest request) {
        Doctor doctor = doctors.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found."));
        doctor.getUser().setName(request.name());
        doctor.getUser().setEmail(request.email());
        doctor.setSpecialization(request.specialization());
        doctor.setQualification(request.qualification());
        doctor.setPhone(request.phone());
        doctor.setAvailableDays(request.availableDays());
        doctor.setStartTime(request.startTime());
        doctor.setEndTime(request.endTime());
        return doctors.save(doctor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Doctor doctor = doctors.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found."));
        if (appointments.existsByDoctorId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctors with appointment history cannot be deleted.");
        doctors.delete(doctor);
        users.delete(doctor.getUser());
    }

    private Doctor toDoctor(User user, DoctorRequest request) {
        return new Doctor(user, request.specialization(), request.qualification(), request.phone(), request.availableDays(), request.startTime(), request.endTime());
    }
}
