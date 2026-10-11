package com.medicare.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.medicare.model.Patient;
import com.medicare.model.User;
import com.medicare.repository.AppointmentRepository;
import com.medicare.repository.DoctorRepository;
import com.medicare.repository.PatientRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {
    @Mock private PatientRepository patients;
    @Mock private DoctorRepository doctors;
    @Mock private AppointmentRepository appointments;

    @Test
    void patientOwnershipRequiresTheAuthenticatedUserIdToMatch() {
        AuthorizationService authorization = new AuthorizationService(patients, doctors, appointments);
        User owner = org.mockito.Mockito.mock(User.class);
        Patient patient = org.mockito.Mockito.mock(Patient.class);
        when(patients.findById(12L)).thenReturn(Optional.of(patient));
        when(patient.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(42L);

        assertTrue(authorization.ownsPatient(12L, authentication("42")));
        assertFalse(authorization.ownsPatient(12L, authentication("43")));
    }

    @Test
    void currentPatientUsesTheAuthenticatedUserId() {
        AuthorizationService authorization = new AuthorizationService(patients, doctors, appointments);
        Patient patient = org.mockito.Mockito.mock(Patient.class);
        when(patients.findByUserId(42L)).thenReturn(Optional.of(patient));

        assertSame(patient, authorization.currentPatient(authentication("42")).orElseThrow());
    }

    private JwtAuthenticationToken authentication(String subject) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}
