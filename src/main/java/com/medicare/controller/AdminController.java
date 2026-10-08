package com.medicare.controller;

import com.medicare.model.AppointmentStatus;
import com.medicare.repository.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final AppointmentRepository appointments;
    public AdminController(PatientRepository patients, DoctorRepository doctors, AppointmentRepository appointments) {
        this.patients = patients;
        this.doctors = doctors;
        this.appointments = appointments;
    }

    @GetMapping("/summary")
    public Map<String, Long> summary() {
        return Map.of("patients", patients.count(), "doctors", doctors.count(), "appointments", appointments.count(), "booked", appointments.countByStatus(AppointmentStatus.BOOKED));
    }
}
