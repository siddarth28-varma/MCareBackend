package com.medicare.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.medicare.model.Appointment;
import com.medicare.model.AppointmentStatus;
import com.medicare.model.Doctor;
import com.medicare.model.Patient;
import com.medicare.repository.AppointmentRepository;
import com.medicare.repository.DoctorRepository;
import com.medicare.repository.PatientRepository;

@Service
public class AppointmentService {
    private final AppointmentRepository appointments;
    private final PatientRepository patients;
    private final DoctorRepository doctors;

    public AppointmentService(AppointmentRepository appointments, PatientRepository patients, DoctorRepository doctors) {
        this.appointments = appointments;
        this.patients = patients;
        this.doctors = doctors;
    }

    @Transactional
    public synchronized Appointment book(Long patientId, Long doctorId, LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose today or a future date.");
        if (appointments.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(doctorId, date, time, AppointmentStatus.BOOKED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That doctor already has an appointment at this time.");
        }
        if (!availableSlots(doctorId, date).contains(time)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose one of the doctor's available appointment slots.");
        Patient patient = patients.findById(patientId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found."));
        Doctor doctor = doctors.findById(doctorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found."));
        return appointments.save(new Appointment(patient, doctor, date, time));
    }

    @Transactional
    public Appointment cancel(Long appointmentId) {
        Appointment appointment = appointments.findById(appointmentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));
        if (appointment.getStatus() != AppointmentStatus.BOOKED) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only booked appointments can be cancelled.");
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointment;
    }

    @Transactional
    public Appointment complete(Long appointmentId) {
        Appointment appointment = appointments.findById(appointmentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found."));
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointment;
    }

    public List<LocalTime> availableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctors.findById(doctorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found."));
        String day = date.getDayOfWeek().name().substring(0, 3);
        if (doctor.getAvailableDays() != null && !doctor.getAvailableDays().isBlank()
            && java.util.Arrays.stream(doctor.getAvailableDays().split(",")).map(String::trim).noneMatch(day::equalsIgnoreCase)) {
            return List.of();
        }
        List<LocalTime> booked = appointments.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctorId).stream()
            .filter(item -> item.getAppointmentDate().equals(date) && item.getStatus() == AppointmentStatus.BOOKED)
            .map(Appointment::getAppointmentTime).toList();
        java.util.ArrayList<LocalTime> slots = new java.util.ArrayList<>();
        for (LocalTime time = doctor.getStartTime(); time.isBefore(doctor.getEndTime()); time = time.plusHours(1)) {
            if (!booked.contains(time)) slots.add(time);
        }
        return slots;
    }
}
