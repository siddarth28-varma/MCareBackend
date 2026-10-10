package com.medicare.dto;

import com.medicare.model.Role;

public record UserResponse(Long id, String name, String email, Role role) {
}
