package com.medicare.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequest(@NotNull Long patientId, @NotNull Long doctorId, @NotNull LocalDate appointmentDate, @NotNull LocalTime appointmentTime) { }
