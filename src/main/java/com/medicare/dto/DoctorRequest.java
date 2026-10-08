package com.medicare.dto;

import java.time.LocalTime;

public record DoctorRequest(String name, String email, String password, String specialization, String qualification, String phone, String availableDays, LocalTime startTime, LocalTime endTime) { }
