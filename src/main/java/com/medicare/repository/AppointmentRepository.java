package com.medicare.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medicare.model.Appointment;
import com.medicare.model.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(Long doctorId);
    List<Appointment> findAllByOrderByAppointmentDateDescAppointmentTimeDesc();
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(Long doctorId, LocalDate date, LocalTime time, AppointmentStatus status);
    boolean existsByDoctorId(Long doctorId);
    long countByStatus(AppointmentStatus status);
}
