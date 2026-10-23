package com.pythogorean_apis.pythogorean_apis.auth;

import com.pythogorean_apis.pythogorean_apis.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record RegisterRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotNull Role role,
            Long classId) {
    }

    public record LoginResponse(String token, String name, String email, Role role) {
    }

    public record RegisterResponse(Long id, String name, String email, Role role, Integer markerId) {
    }
}
