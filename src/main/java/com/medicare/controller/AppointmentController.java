package com.medicare.controller;

import com.medicare.dto.AppointmentRequest;
import com.medicare.model.Appointment;
import com.medicare.repository.AppointmentRepository;
import com.medicare.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentRepository appointments;
    private final AppointmentService appointmentService;
    public AppointmentController(AppointmentRepository appointments, AppointmentService appointmentService) {
        this.appointments = appointments;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> all() { return appointments.findAllByOrderByAppointmentDateDescAppointmentTimeDesc(); }
    @GetMapping("/patient/{patientId}")
    public List<Appointment> byPatient(@PathVariable Long patientId) { return appointments.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patientId); }
    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> byDoctor(@PathVariable Long doctorId) { return appointments.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctorId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment book(@Valid @RequestBody AppointmentRequest request) {
        return appointmentService.book(request.patientId(), request.doctorId(), request.appointmentDate(), request.appointmentTime());
    }

    @PatchMapping("/{id}/cancel")
    public Appointment cancel(@PathVariable Long id) { return appointmentService.cancel(id); }
    @PatchMapping("/{id}/complete")
    public Appointment complete(@PathVariable Long id) { return appointmentService.complete(id); }
}
