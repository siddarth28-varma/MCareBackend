package com.medicare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email, @NotBlank String password, String phone, String dateOfBirth, String gender) { }
