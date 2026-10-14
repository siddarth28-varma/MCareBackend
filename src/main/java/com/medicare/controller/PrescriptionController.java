package com.medicare.controller;

import com.medicare.dto.ConsultationRequest;
import com.medicare.model.Appointment;
import com.medicare.model.Prescription;
import com.medicare.repository.AppointmentRepository;
import com.medicare.repository.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appointments/{appointmentId}/consultation")
public class PrescriptionController {
    private final AppointmentRepository appointments;
    private final PrescriptionRepository prescriptions;
    public PrescriptionController(AppointmentRepository appointments, PrescriptionRepository prescriptions) {
        this.appointments = appointments;
        this.prescriptions = prescriptions;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @authorization.isAssignedDoctor(#p0, authentication))")
    public Prescription get(@PathVariable Long appointmentId) {
        return prescriptions.findByAppointmentId(appointmentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultation not found."));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @authorization.isAssignedDoctor(#p0, authentication))")
    public Prescription save(@PathVariable Long appointmentId, @RequestBody ConsultationRequest request) {
        Appointment appointment = appointments.findById(appointmentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));
        Prescription record = prescriptions.findByAppointmentId(appointmentId).orElseGet(() -> new Prescription(appointment, request.consultationNotes(), request.prescriptionDetails()));
        record.setConsultationNotes(request.consultationNotes());
        record.setPrescriptionDetails(request.prescriptionDetails());
        record.setUpdatedAt(LocalDateTime.now());
        return prescriptions.save(record);
    }
}
