package com.medicare.security;

import com.medicare.model.Appointment;
import com.medicare.model.Doctor;
import com.medicare.model.Patient;
import com.medicare.repository.AppointmentRepository;
import com.medicare.repository.DoctorRepository;
import com.medicare.repository.PatientRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("authorization")
public class AuthorizationService {
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final AppointmentRepository appointments;

    public AuthorizationService(PatientRepository patients, DoctorRepository doctors, AppointmentRepository appointments) {
        this.patients = patients;
        this.doctors = doctors;
        this.appointments = appointments;
    }

    @Transactional(readOnly = true)
    public boolean ownsPatient(Long patientId, Authentication authentication) {
        return currentUserId(authentication).flatMap(userId -> patients.findById(patientId)
                .map(Patient::getUser)
                .map(user -> user.getId().equals(userId))).orElse(false);
    }

    public boolean ownsUser(Long userId, Authentication authentication) {
        return currentUserId(authentication).map(id -> id.equals(userId)).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean ownsDoctor(Long doctorId, Authentication authentication) {
        return currentUserId(authentication).flatMap(userId -> doctors.findById(doctorId)
                .map(Doctor::getUser)
                .map(user -> user.getId().equals(userId))).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean ownsAppointment(Long appointmentId, Authentication authentication) {
        return currentUserId(authentication).flatMap(userId -> appointments.findById(appointmentId)
                .map(appointment -> appointment.getPatient().getUser().getId().equals(userId)
                        || appointment.getDoctor().getUser().getId().equals(userId))).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isAssignedDoctor(Long appointmentId, Authentication authentication) {
        return currentUserId(authentication).flatMap(userId -> appointments.findById(appointmentId)
                .map(Appointment::getDoctor)
                .map(Doctor::getUser)
                .map(user -> user.getId().equals(userId))).orElse(false);
    }

    private Optional<Long> currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(jwt.getSubject()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
