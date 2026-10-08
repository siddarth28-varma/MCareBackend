package com.medicare.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.medicare.model.AppointmentStatus;
import com.medicare.model.Doctor;
import com.medicare.model.Role;
import com.medicare.model.User;
import com.medicare.repository.AppointmentRepository;
import com.medicare.repository.DoctorRepository;
import com.medicare.repository.PatientRepository;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock private AppointmentRepository appointments;
    @Mock private PatientRepository patients;
    @Mock private DoctorRepository doctors;
    @InjectMocks private AppointmentService service;

    @Test
    void duplicateBookedSlotReturnsConflict() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(appointments.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(1L, date, LocalTime.of(9, 0), AppointmentStatus.BOOKED)).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> service.book(1L, 1L, date, LocalTime.of(9, 0)));

        assertEquals(409, error.getStatusCode().value());
        verifyNoInteractions(patients, doctors);
    }

    @Test
    void pastAppointmentDateIsRejected() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> service.book(1L, 1L, LocalDate.now().minusDays(1), LocalTime.of(9, 0)));

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(appointments, patients, doctors);
    }

    @Test
    void availableSlotsOnlyAppearOnConfiguredWorkingDays() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        Doctor doctor = new Doctor(new User("Dr. Demo", "demo@example.com", "demo123", Role.DOCTOR), "Cardiology", "MBBS", "", "MON", LocalTime.of(9, 0), LocalTime.of(11, 0));
        when(doctors.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointments.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(1L)).thenReturn(List.of());

        assertEquals(List.of(LocalTime.of(9, 0), LocalTime.of(10, 0)), service.availableSlots(1L, monday));
    }

}
